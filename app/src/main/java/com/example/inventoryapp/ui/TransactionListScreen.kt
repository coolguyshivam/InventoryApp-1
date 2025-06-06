package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

data class TransactionRecord(
    val id: String = "",
    val itemName: String = "",
    val serialNumber: String = "",
    val customerName: String = "",
    val timestamp: Long = 0L,
    val type: String = "Sale",
    val imageUrls: List<String> = emptyList()
)

@Composable
fun TransactionListScreen() {
    var transactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var selectedTransaction by remember { mutableStateOf<TransactionRecord?>(null) }

    val db = FirebaseFirestore.getInstance()

    // Load Firestore transactions (sales + purchases)
    LaunchedEffect(Unit) {
        val sales = db.collection("sales").get().await().documents.mapNotNull {
            it.toTransactionRecord("Sale")
        }
        val purchases = db.collection("purchases").get().await().documents.mapNotNull {
            it.toTransactionRecord("Purchase")
        }

        transactions = (sales + purchases).sortedByDescending { it.timestamp }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Transaction History", style = MaterialTheme.typography.titleLarge)

        LazyColumn {
            items(transactions) { txn ->
                val cardColor = if (txn.type == "Sale") Color(0xFFFFE0E0) else Color(0xFFD0F5DC)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedTransaction = txn },
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Item: ${txn.itemName}")
                        Text("Serial: ${txn.serialNumber}")
                        Text("Customer: ${txn.customerName}")
                        Text("Type: ${txn.type}")
                        Text("Date: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm").format(java.util.Date(txn.timestamp))}")
                        if (txn.imageUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                txn.imageUrls.take(3).forEach { url ->
                                    Image(
                                        painter = rememberAsyncImagePainter(url),
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp).padding(end = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detail dialog
        selectedTransaction?.let { txn ->
            AlertDialog(
                onDismissRequest = { selectedTransaction = null },
                confirmButton = {
                    TextButton(onClick = { selectedTransaction = null }) { Text("Close") }
                },
                title = { Text("Transaction Details") },
                text = {
                    Column {
                        Text("Item: ${txn.itemName}")
                        Text("Serial: ${txn.serialNumber}")
                        Text("Customer: ${txn.customerName}")
                        Text("Type: ${txn.type}")
                        Text("Date: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm").format(java.util.Date(txn.timestamp))}")
                        if (txn.imageUrls.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Attached Images:")
                            Row {
                                txn.imageUrls.forEach { url ->
                                    Image(
                                        painter = rememberAsyncImagePainter(url),
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp).padding(end = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun QueryDocumentSnapshot.toTransactionRecord(type: String): TransactionRecord? {
    val serial = getString("serialNumber") ?: return null
    val item = getString("itemName") ?: ""
    val customer = getString("customerName") ?: ""
    val ts = getLong("timestamp") ?: 0L
    val urls = get("imageUrls") as? List<String> ?: emptyList()

    return TransactionRecord(
        id = id,
        itemName = item,
        serialNumber = serial,
        customerName = customer,
        timestamp = ts,
        type = type,
        imageUrls = urls
    )
}
