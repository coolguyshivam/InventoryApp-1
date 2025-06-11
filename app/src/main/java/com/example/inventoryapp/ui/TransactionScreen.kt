package com.example.inventoryapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()

    // Parse serial and type from nav args like: transaction?serial=123&type=Sale
    val navBackStackEntry = navController.currentBackStackEntry
    val serialArg = navBackStackEntry?.arguments?.getString("serial") ?: ""
    val typeArg = navBackStackEntry?.arguments?.getString("type") ?: "Purchase"

    var transactionType by remember { mutableStateOf(typeArg) }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf(serialArg) }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var quantity by remember { mutableStateOf("1") }
    var error by remember { mutableStateOf("") }

    // Auto-fill model for sale
    LaunchedEffect(serial, transactionType) {
        if (transactionType == "Sale" && serial.isNotBlank()) {
            db.collection("transactions")
                .whereEqualTo("serial", serial)
                .whereEqualTo("type", "Purchase")
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        model = it.documents[0].getString("model") ?: ""
                    } else {
                        model = ""
                        error = "Item not in inventory"
                    }
                }
                .addOnFailureListener {
                    error = "Error fetching item details"
                }
        }
    }

    fun validateAndSubmit() {
        error = ""
        if (model.isBlank() || serial.isBlank() || amount.isBlank()) {
            error = "Model, Serial and Amount are required"
            return
        }
        if (phone.isNotBlank() && phone.length != 10) {
            error = "Phone number must be 10 digits"
            return
        }
        if (aadhaar.isNotBlank() && aadhaar.length != 12) {
            error = "Aadhaar must be 12 digits"
            return
        }

        if (transactionType == "Purchase") {
            db.collection("transactions")
                .whereEqualTo("serial", serial)
                .whereEqualTo("type", "Purchase")
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        saveTransaction()
                    } else {
                        error = "Serial already exists in inventory"
                    }
                }
        } else {
            db.collection("transactions")
                .whereEqualTo("serial", serial)
                .whereEqualTo("type", "Purchase")
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        error = "Item not found in inventory for sale"
                    } else {
                        saveTransaction()
                    }
                }
        }
    }

    fun saveTransaction() {
        val data = hashMapOf(
            "type" to transactionType,
            "model" to model,
            "serial" to serial,
            "phone" to phone,
            "aadhaar" to aadhaar,
            "amount" to amount.toDoubleOrNull(),
            "description" to description,
            "date" to date,
            "quantity" to quantity.toIntOrNull() ?: 1,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("transactions").add(data)
            .addOnSuccessListener {
                error = ""
                model = ""
                serial = ""
                phone = ""
                aadhaar = ""
                amount = ""
                description = ""
                quantity = "1"
                navController.popBackStack() // go back
            }
            .addOnFailureListener {
                error = "Failed to save transaction"
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Transaction Type", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Purchase", "Sale").forEach {
                Button(
                    onClick = { transactionType = it },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (transactionType == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(it)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Serial Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = aadhaar, onValueChange = { aadhaar = it }, label = { Text("Aadhaar Number") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

        if (error.isNotBlank()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = { validateAndSubmit() }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Submit")
        }
    }
}
