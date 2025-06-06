package com.example.inventoryapp.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TransactionRecord(
    val transactionType: String = "",
    val serialNumber: String = "",
    val itemName: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val aadhaarNumber: String = "",
    val amount: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    val imageUrls: List<String> = emptyList()
)

@Composable
fun TransactionListScreen() {
    val db = FirebaseFirestore.getInstance()
    var transactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val snapshot = db.collection("transactions")
                .orderBy("timestamp")
                .get()
                .await()

            val data = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TransactionRecord::class.java)
            }

            transactions = data.reversed() // latest first
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(transactions) { transaction ->
            TransactionCard(transaction)
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionRecord) {
    val isSale = transaction.transactionType.equals("Sale", ignoreCase = true)
    val cardColor = if (isSale) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Type: ${transaction.transactionType}", fontWeight = FontWeight.Bold)
            Text("Serial: ${transaction.serialNumber}")
            Text("Item: ${transaction.itemName}")
            Text("Customer: ${transaction.customerName}")
            Text("Phone: ${transaction.phoneNumber}")
            Text("Aadhaar: ${transaction.aadhaarNumber}")
            Text("Amount: ₹${transaction.amount}")
            Text("Date: ${transaction.date}")

            if (transaction.imageUrls.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    transaction.imageUrls.take(3).forEach { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}
