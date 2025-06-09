package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventoryapp.model.TransactionRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionListScreen() {
    val db = FirebaseFirestore.getInstance()
    var transactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            transactions = snapshot.documents.mapNotNull { it.toObject(TransactionRecord::class.java) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "All Transactions",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionCard(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionCard(record: TransactionRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.transactionType.lowercase() == "sale") MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Type: ${record.transactionType}", style = MaterialTheme.typography.bodyLarge)
            Text("Item: ${record.itemName}", style = MaterialTheme.typography.bodyMedium)
            Text("Serial No: ${record.serialNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Customer: ${record.customerName}", style = MaterialTheme.typography.bodyMedium)
            Text("Phone: ${record.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Amount: ₹${record.amount}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${record.date}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
