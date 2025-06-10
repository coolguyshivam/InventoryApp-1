package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.inventoryapp.model.TransactionRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.example.inventoryapp.ui.InventoryScreen

@Composable
fun TransactionListScreen() {
    val db = FirebaseFirestore.getInstance()
    var allTransactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var filteredTransactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = db.collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            allTransactions = snapshot.documents.mapNotNull { it.toObject(TransactionRecord::class.java) }
            filteredTransactions = allTransactions
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            "Transactions Report",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredTransactions = allTransactions.filter { record ->
                    val q = it.text.trim().lowercase()
                    record.itemName.lowercase().contains(q)
                        || record.serialNumber.lowercase().contains(q)
                        || record.customerName.lowercase().contains(q)
                        || record.phoneNumber.contains(q)
                        || record.aadhaarNumber.contains(q)
                }
            },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(filteredTransactions) { record ->
                    TransactionCardWithImages(record)
                }
            }
        }
    }
}

@Composable
fun TransactionCardWithImages(record: TransactionRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.transactionType.lowercase() == "sale")
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Type: ${record.transactionType}", style = MaterialTheme.typography.bodyLarge)
            Text("Item: ${record.itemName}")
            Text("Serial No: ${record.serialNumber}")
            Text("Customer: ${record.customerName}")
            Text("Phone: ${record.phoneNumber}")
            Text("Aadhaar: ${record.aadhaarNumber}")
            Text("Amount: ₹${record.amount}")
            Text("Date: ${record.date}")

            // Images
            if (record.imageUrls.isNotEmpty()) {
                Row(Modifier.padding(top = 8.dp)) {
                    record.imageUrls.take(3).forEach { imageUrl ->
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Attachment",
                            modifier = Modifier
                                .size(64.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
