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
) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val scope = rememberCoroutineScope()

    var transactionType by remember { mutableStateOf(defaultType ?: "Sale") }
    var serialNumber by remember { mutableStateOf(defaultSerial ?: "") }
    var itemName by remember { mutableStateOf(defaultItem ?: "") }
    var customerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var aadhaarNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            imageUris = (imageUris + uris).take(3)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Purchase", "Sale").forEach { type ->
                OutlinedButton(
                    onClick = { transactionType = type },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == type)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(type)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it.trim() },
            label = { Text("Serial Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = aadhaarNumber,
            onValueChange = { aadhaarNumber = it },
            label = { Text("Aadhaar Number") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (₹)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Preview images
        if (imageUris.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                imageUris.forEach {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        TextButton(
            onClick = { imagePicker.launch("image/*") },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Attach Photos (max 3)")
        }

        if (error != null) {
            Text(error ?: "", color = MaterialTheme.colorScheme.error)
        }

        Button(
			onClick = {
				scope.launch {
					error = null
		
					if (serialNumber.isBlank() || itemName.isBlank() || amount.isBlank()) {
						error = "Please fill all required fields."
						return@launch
					}
		
					val parsedAmount = amount.toIntOrNull()
					val parsedQuantity = quantity.toIntOrNull() ?: 1
					if (parsedAmount == null) {
						error = "Amount must be numeric"
						return@launch
					}
		
					val inventoryRef = db.collection("inventory").document(serialNumber.trim())
		
					try {
						val existingDoc = inventoryRef.get().await()
		
						if (transactionType == "Purchase" && existingDoc.exists()) {
							error = "This serial number already exists."
							return@launch
						}
						if (transactionType == "Sale" && !existingDoc.exists()) {
							error = "This serial number does not exist in inventory."
							return@launch
						}
		
						val imageUrls = mutableListOf<String>()
						for (uri in imageUris) {
							try {
								val ref = storage.reference.child("transactions/${UUID.randomUUID()}.jpg")
								ref.putFile(uri).await()
								val url = ref.downloadUrl.await().toString()
								imageUrls.add(url)
							} catch (e: Exception) {
								error = "Image upload failed: ${e.message}"
								return@launch
							}
						}
		
						val txnData = mapOf(
							"transactionType" to transactionType,
							"serialNumber" to serialNumber.trim(),
							"itemName" to itemName.trim(),
							"customerName" to customerName.trim(),
							"phoneNumber" to phoneNumber.trim(),
							"aadhaarNumber" to aadhaarNumber.trim(),
							"amount" to parsedAmount,
							"quantity" to parsedQuantity,
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
									"phoneNumber" to phoneNumber.trim(),
									"aadhaarNumber" to aadhaarNumber.trim(),
									"timestamp" to System.currentTimeMillis(),
									"isSold" to false
								)
							).await()
						} else {
							inventoryRef.update("isSold", true).await()
						}
		
						// Reset form
						serialNumber = ""
						itemName = ""
						customerName = ""
						phoneNumber = ""
						aadhaarNumber = ""
						amount = ""
						quantity = "1"
						imageUris = emptyList()
		
					} catch (e: Exception) {
						error = "Transaction failed: ${e.message}"
					}
				}
			},
			modifier = Modifier.fillMaxWidth()
		) {
    Text("Submit")
	}
	}
}
