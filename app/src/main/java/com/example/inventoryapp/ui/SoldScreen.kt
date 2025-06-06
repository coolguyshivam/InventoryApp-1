package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@Composable
fun SoldScreen() {
    val db = FirebaseFirestore.getInstance()
    val salesCollection = db.collection("transactions").whereEqualTo("transactionType", "Sale").orderBy("timestamp", Query.Direction.DESCENDING)

    var soldItems by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var lastVisible by remember { mutableStateOf<DocumentSnapshot?>(null) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Initial load
    LaunchedEffect(Unit) {
        salesCollection.limit(200).addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                soldItems = it.documents.mapNotNull { doc -> doc.toObject(TransactionRecord::class.java) }
                lastVisible = it.documents.lastOrNull()
            }
        }
    }

    // Load more on scroll
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val total = listState.layoutInfo.totalItemsCount
        if (listState.firstVisibleItemIndex + 5 >= total && !isLoadingMore && lastVisible != null) {
            isLoadingMore = true
            salesCollection.startAfter(lastVisible!!).limit(100).get().addOnSuccessListener { snapshot ->
                val moreItems = snapshot.documents.mapNotNull { doc -> doc.toObject(TransactionRecord::class.java) }
                soldItems = soldItems + moreItems
                lastVisible = snapshot.documents.lastOrNull()
                isLoadingMore = false
            }
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(soldItems.size) { index ->
            val transaction = soldItems[index]
            SoldItemCard(transaction)
        }
    }
}

@Composable
fun SoldItemCard(transaction: TransactionRecord) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Sold: ${transaction.itemName}")
            Text("Serial: ${transaction.serialNumber}")
            Text("Customer: ${transaction.customerName}")
            Text("Amount: ₹${transaction.amount}")
            Text("Date: ${transaction.date}")

            if (transaction.imageUrls.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    transaction.imageUrls.take(3).forEach { url ->
                        Image(
                            painter = rememberAsyncImagePainter(url),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        }
    }
}
