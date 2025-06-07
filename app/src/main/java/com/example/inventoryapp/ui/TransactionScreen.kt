package com.example.inventoryapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun TransactionScreen(
    navController: NavHostController,
    defaultType: String? = null,
    defaultSerial: String? = null,
    defaultItem: String? = null
)
 {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var transactionType by remember { mutableStateOf(defaultType ?: "Sale") }
	var serialNumber by remember { mutableStateOf(defaultSerial ?: "") }
	var itemName by remember { mutableStateOf(defaultItem ?: "") }
    var customerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var error by remember { mutableStateOf<String?>(null) }

    val imageUris = remember { mutableStateListOf<Uri>() }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null && imageUris.size + uris.size <= 3) {
            imageUris.addAll(uris)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())) {

        Row {
            Button(onClick = { transactionType = "Purchase" }, colors = ButtonDefaults.buttonColors(if (transactionType == "Purchase") Color.Green else Color.Gray)) {
                Text("Purchase")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { transactionType = "Sale" }, colors = ButtonDefaults.buttonColors(if (transactionType == "Sale") Color.Red else Color.Gray)) {
                Text("Sale")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("Serial Number") })
        OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") })
        OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") })
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        OutlinedTextField(value = aadhaarNumber, onValueChange = { aadhaarNumber = it }, label = { Text("Aadhaar Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = amount, onValueChange = { amount = it.filter { it.isDigit() } }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { it.isDigit() } }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Spacer(Modifier.height(12.dp))
        Button(onClick = { imageLauncher.launch("image/*") }) {
            Text("Attach Images (${imageUris.size}/3)")
        }

        imageUris.forEach {
            Image(painter = rememberAsyncImagePainter(it), contentDescription = null, modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(16.dp))

        error?.let {
            Text(it, color = Color.Red)
        }

        Button(onClick = {
            scope.launch {
                error = null
                if (serialNumber.isBlank() || itemName.isBlank() || amount.isBlank()) {
                    error = "Please fill all required fields."
                    return@launch
                }

                val inventoryRef = db.collection("inventory").document(serialNumber)
                val doc = inventoryRef.get().await()

                if (transactionType == "Purchase" && doc.exists()) {
                    error = "Item with this serial number already exists in inventory."
                    return@launch
                }

                if (transactionType == "Sale" && !doc.exists()) {
                    error = "This item is not available in inventory."
                    return@launch
                }

                val imageUrls = mutableListOf<String>()
                for (uri in imageUris) {
                    val ref = storage.reference.child("transactions/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    val url = ref.downloadUrl.await().toString()
                    imageUrls.add(url)
                }

                val txnData = mapOf(
                    "transactionType" to transactionType,
                    "serialNumber" to serialNumber.trim(),
                    "itemName" to itemName.trim(),
                    "customerName" to customerName.trim(),
                    "phoneNumber" to phoneNumber.trim(),
                    "aadhaarNumber" to aadhaarNumber.trim(),
                    "amount" to amount.toIntOrNull(),
                    "quantity" to quantity.toIntOrNull(),
                    "timestamp" to System.currentTimeMillis(),
                    "imageUrls" to imageUrls
                )

                db.collection("transactions").add(txnData).await()

                if (transactionType == "Purchase") {
                    inventoryRef.set(
                        mapOf(
                            "serialNumber" to serialNumber.trim(),
                            "itemName" to itemName.trim(),
                            "customerName" to customerName.trim(),
                            "timestamp" to System.currentTimeMillis(),
                            "isSold" to false
                        )
                    )
                } else if (transactionType == "Sale") {
                    inventoryRef.update("isSold", true)
                }

                // reset
                serialNumber = ""
                itemName = ""
                customerName = ""
                phoneNumber = ""
                aadhaarNumber = ""
                amount = ""
                quantity = "1"
                imageUris.clear()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Submit")
        }
    }
 }
