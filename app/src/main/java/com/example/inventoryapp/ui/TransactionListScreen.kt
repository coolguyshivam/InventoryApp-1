package com.example.inventoryapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionListScreen() {
    val db = FirebaseFirestore.getInstance()
    var transactions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var filter by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("transactions").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                transactions = it.documents.mapNotNull { doc ->
                    doc.data?.plus("id" to doc.id)
                }.sortedByDescending { it["timestamp"] as? Long ?: 0L }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            label = { Text("Search (model, serial, phone, aadhaar, description)") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filtered = transactions.filter {
                val query = filter.trim().lowercase()
                query.isEmpty() || listOf(
                    it["model"], it["serial"], it["phone"], it["aadhaar"], it["description"]
                ).any { field -> field?.toString()?.lowercase()?.contains(query) == true }
            }

            items(filtered) { txn ->
                val isSale = txn["type"] == "Sale"
                val bgColor = if (isSale) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                val headerColor = if (isSale) Color(0xFFD32F2F) else Color(0xFF388E3C)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(bgColor),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "${txn["type"]} - ${txn["model"]}",
                            color = headerColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Serial: ${txn["serial"]}")
                        Text("Phone: ${txn["phone"]}")
                        Text("Aadhaar: ${txn["aadhaar"]}")
                        Text("Amount: ₹${txn["amount"]}")
                        Text("Date: ${txn["date"]}")
                        Text("Quantity: ${txn["quantity"]}")
                        Text("Desc: ${txn["description"]}")
                        Text(
                            "Time: ${
                                SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                                    .format(Date(txn["timestamp"] as Long))
                            }"
                        )
                    }
                }
            }
        }
    }
}
