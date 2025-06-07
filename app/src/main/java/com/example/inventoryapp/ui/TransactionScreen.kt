package com.example.inventoryapp.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
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
    var quantity by remember { mutableStateOf("1") }

    val imageUris = remember { mutableStateListOf<Uri>() }
    val uploadProgress = remember { mutableStateListOf<Boolean>() }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.take(3 - imageUris.size).forEach {
            imageUris.add(it)
            uploadProgress.add(false)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Transaction: $transactionType", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Serial Number") })
        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") })
        OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") })
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") })
        OutlinedTextField(value = aadhaarNumber, onValueChange = { aadhaarNumber = it }, label = { Text("Aadhaar Number") })
        OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = { Text("Amount") })
        OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } }, label = { Text("Quantity") })

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { imageLauncher.launch("image/*") }) {
                Text("Attach Images")
            }

            Button(onClick = {
                if (uploadProgress.any { !it }) {
                    // Block submission while uploading
                    return@Button
                }

                scope.launch {
                    val urls = mutableListOf<String>()
                    imageUris.forEachIndexed { index, uri ->
                        uploadProgress[index] = true
                        val url = uploadImageToFirebase(context, uri, storage)
                        uploadProgress[index] = false
                        if (url != null) urls.add(url)
                    }

                    val txnData = hashMapOf(
                        "type" to transactionType,
                        "serialNumber" to serialNumber,
                        "itemName" to itemName,
                        "customerName" to customerName,
                        "phoneNumber" to phoneNumber,
                        "aadhaarNumber" to aadhaarNumber,
                        "amount" to amount,
                        "quantity" to quantity,
                        "timestamp" to System.currentTimeMillis(),
                        "imageUrls" to urls
                    )

                    db.collection("transactions").add(txnData)
                }
            }) {
                Text("Submit")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        imageUris.forEach { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Attachment",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 4.dp)
            )
        }
    }
}

suspend fun uploadImageToFirebase(context: Context, uri: Uri, storage: FirebaseStorage): String? {
    val fileName = UUID.randomUUID().toString() + ".jpg"
    val ref = storage.reference.child("transactions/$fileName")
    val uploadTask = ref.putFile(uri).await()
    return if (uploadTask.task.isSuccessful) {
        ref.downloadUrl.await().toString()
    } else null
}
