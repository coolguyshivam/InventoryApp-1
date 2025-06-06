package com.example.inventoryapp.ui

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun TransactionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var transactionType by remember { mutableStateOf("Sale") }
    var serialNumber by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        imageUris = uris.take(3)
    }

    val scannedSerial = navController.currentBackStackEntry?.savedStateHandle?.get<String>("saleSerial")
    val prefilledItem = navController.currentBackStackEntry?.savedStateHandle?.get<String>("saleItem")

    if (!scannedSerial.isNullOrEmpty()) {
        serialNumber = scannedSerial
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("saleSerial")
    }

    if (!prefilledItem.isNullOrEmpty()) {
        itemName = prefilledItem
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("saleItem")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Transaction Type", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RadioButton(selected = transactionType == "Sale", onClick = { transactionType = "Sale" })
            Text("Sale")
            RadioButton(selected = transactionType == "Purchase", onClick = { transactionType = "Purchase" })
            Text("Purchase")
        }

        OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Serial Number") })
        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") })
        OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") })
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") })
        OutlinedTextField(value = aadhaarNumber, onValueChange = { aadhaarNumber = it }, label = { Text("Aadhaar Number") })
        OutlinedTextField(value = amount, onValueChange = { if (it.all(Char::isDigit)) amount = it }, label = { Text("Amount") })

        Spacer(modifier = Modifier.height(12.dp))
        Text("Attach up to 3 photos:")
        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Select Images")
        }

        LazyRow {
            items(imageUris.size) { i ->
                Image(
                    painter = rememberAsyncImagePainter(imageUris[i]),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).padding(end = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val collection = if (transactionType == "Sale") "sales" else "purchases"
            val docId = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("$collection/$docId")

            val uploadedUrls = mutableListOf<String>()

            LaunchedEffect(imageUris) {
                try {
                    for ((index, uri) in imageUris.withIndex()) {
                        val ext = MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "jpg"
                        val ref = storageRef.child("photo_$index.$ext")
                        ref.putFile(uri).await()
                        uploadedUrls.add(ref.downloadUrl.await().toString())
                    }

                    val txn = mapOf(
                        "serialNumber" to serialNumber,
                        "itemName" to itemName,
                        "customerName" to customerName,
                        "phoneNumber" to phoneNumber,
                        "aadhaarNumber" to aadhaarNumber,
                        "price" to amount.toDouble(),
                        "timestamp" to System.currentTimeMillis(),
                        "imageUrls" to uploadedUrls
                    )

                    db.collection(collection).document(docId).set(txn)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Submit Transaction")
        }
    }
}
