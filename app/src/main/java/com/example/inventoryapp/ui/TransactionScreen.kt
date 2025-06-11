package com.example.inventoryapp.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun TransactionScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()

    // Parse serial if passed from scanner
    val serialFromArgs = navController.currentBackStackEntry?.arguments?.getString("serial")

    var transactionType by remember { mutableStateOf("Purchase") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf(serialFromArgs ?: "") }
    var phone by remember { mutableStateOf("") }
    var aadhaar by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var quantity by remember { mutableStateOf("1") }
    var error by remember { mutableStateOf("") }

    val context = LocalContext.current

    fun saveTransaction() {
        val transaction = hashMapOf(
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

        db.collection("transactions").add(transaction)
        error = ""
        model = ""
        serial = ""
        phone = ""
        aadhaar = ""
        amount = ""
        description = ""
        quantity = "1"
    }

    fun validateAndSubmit() {
        if (model.isBlank() || serial.isBlank() || amount.isBlank()) {
            error = "Model, Serial and Amount are required"
            return
        }

        val isPurchase = transactionType == "Purchase"
        db.collection("transactions")
            .whereEqualTo("serial", serial)
            .whereEqualTo("type", "Purchase")
            .get()
            .addOnSuccessListener { result ->
                val canSave = (isPurchase && result.isEmpty) || (!isPurchase && !result.isEmpty)
                if (canSave) {
                    saveTransaction()
                } else {
                    error = if (isPurchase) {
                        "Item with this serial already exists in inventory"
                    } else {
                        "No such item in inventory to sell"
                    }
                }
            }
    }

    // Auto-fill model if sale and serial is valid
    LaunchedEffect(serial, transactionType) {
        if (transactionType == "Sale" && serial.isNotBlank()) {
            db.collection("transactions")
                .whereEqualTo("serial", serial)
                .whereEqualTo("type", "Purchase")
                .limit(1)
                .get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        val purchaseData = it.documents[0].data
                        model = purchaseData?.get("model")?.toString() ?: ""
                    } else {
                        model = ""
                        error = "Item not in inventory"
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = serial,
            onValueChange = { serial = it },
            label = { Text("Serial Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = aadhaar,
            onValueChange = { aadhaar = it },
            label = { Text("Aadhaar Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { validateAndSubmit() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Submit")
        }
    }
}