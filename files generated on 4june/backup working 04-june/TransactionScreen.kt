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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.*
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()

    var transactionType by remember { mutableStateOf("Sale") }
    var serialNumber by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        imageUris = uris.take(3)
        if (imageUris.isNotEmpty()) {
            uploadImages(imageUris, transactionType, context, storage) { resultUrls, uploading ->
                imageUrls = resultUrls
                isUploading = uploading
            }
        }
    }

    val scannedSerial = navController.currentBackStackEntry?.savedStateHandle?.get<String>("scannedSerial")
    val prefilledItem = navController.currentBackStackEntry?.savedStateHandle?.get<String>("prefilledItem")

    if (!scannedSerial.isNullOrEmpty()) {
        serialNumber = scannedSerial
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedSerial")
    }

    if (!prefilledItem.isNullOrEmpty()) {
        itemName = prefilledItem
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("prefilledItem")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Transaction Type", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RadioButton(selected = transactionType == "Sale", onClick = { transactionType = "Sale" })
            Text("Sale")
            RadioButton(selected = transactionType == "Purchase", onClick = { transactionType = "Purchase" })
            Text("Purchase")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Serial Number") })
        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") })
        OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") })
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") })
        OutlinedTextField(value = aadhaarNumber, onValueChange = { aadhaarNumber = it }, label = { Text("Aadhaar Number") })
        OutlinedTextField(value = amount, onValueChange = {
            if (it.all(Char::isDigit)) amount = it
        }, label = { Text("Amount") })

        Spacer(modifier = Modifier.height(12.dp))

        Text("Attach up to 3 images:")
        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Select Images")
        }

        if (imageUris.isNotEmpty()) {
            LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                items(imageUris.size) { index ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUris[index]),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 8.dp)
                    )
                }
            }
        }

        if (isUploading) {
            Text("Uploading images...", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val docId = UUID.randomUUID().toString()
                val collection = if (transactionType == "Sale") "sales" else "purchases"
                val data = hashMapOf(
                    "serialNumber" to serialNumber,
                    "itemName" to itemName,
                    "customerName" to customerName,
                    "phoneNumber" to phoneNumber,
                    "aadhaarNumber" to aadhaarNumber,
                    "price" to amount.toDouble(),
                    "timestamp" to System.currentTimeMillis(),
                    "imageUrls" to imageUrls
                )
                scope.launch {
                    db.collection(collection).document(docId).set(data)
                }
            },
            enabled = !isUploading && serialNumber.isNotBlank() && itemName.isNotBlank() && amount.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Transaction")
        }
    }
}

private fun uploadImages(
    uris: List<Uri>,
    type: String,
    context: android.content.Context,
    storage: FirebaseStorage,
    onResult: (List<String>, Boolean) -> Unit
) {
    val uploaded = mutableListOf<String>()
    var completed = 0
    val total = uris.size
    onResult(emptyList(), true)

    uris.forEachIndexed { index, uri ->
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "jpg"
        val imageRef = storage.reference.child("$type/${UUID.randomUUID()}_img$index.$ext")
        imageRef.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
            imageRef.downloadUrl
        }.addOnSuccessListener { url ->
            uploaded.add(url.toString())
            completed++
            if (completed == total) {
                onResult(uploaded, false)
            }
        }.addOnFailureListener {
            onResult(emptyList(), false)
        }
    }
}
