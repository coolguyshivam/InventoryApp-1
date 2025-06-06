package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*

data class SoldItem(
    val serialNumber: String,
    val itemName: String,
    val customerName: String,
    val timestamp: Long,
    val imageUrls: List<String>
)

@Composable
fun SoldScreen() {
    val db = FirebaseFirestore.getInstance()
    val salesRef = db.collection("sales").orderBy("timestamp", Query.Direction.DESCENDING)

    var soldItems by remember { mutableStateOf(listOf<SoldItem>()) }
    var lastVisible by remember { mutableStateOf<QueryDocumentSnapshot?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Initial load
    LaunchedEffect(Unit) {
        salesRef.limit(200).get().addOnSuccessListener { snapshot ->
            val items = snapshot.documents.mapNotNull { it.toSoldItem() }
            soldItems = items
            lastVisible = snapshot.documents.lastOrNull() as? QueryDocumentSnapshot
        }
    }

    // Pagination
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isLoading &&
            listState.firstVisibleItemIndex >= soldItems.size - 5 &&
            lastVisible != null
        ) {
            isLoading = true
            salesRef.startAfter(lastVisible!!).limit(100).get().addOnSuccessListener { snapshot ->
                val moreItems = snapshot.documents.mapNotNull { it.toSoldItem() }
                soldItems = soldItems + moreItems
                lastVisible = snapshot.documents.lastOrNull() as? QueryDocumentSnapshot
                isLoading = false
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), state = listState) {
        items(soldItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Item: ${item.itemName}")
                    Text("Serial: ${item.serialNumber}")
                    Text("Customer: ${item.customerName}")
                    Text("Date: ${SimpleDateFormat("dd MMM yyyy HH:mm").format(Date(item.timestamp))}")

                    if (item.imageUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            item.imageUrls.take(3).forEach { url ->
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

        if (isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        }
    }
}

fun QueryDocumentSnapshot.toSoldItem(): SoldItem {
    return SoldItem(
        serialNumber = getString("serialNumber") ?: "",
        itemName = getString("itemName") ?: "",
        customerName = getString("customerName") ?: "",
        timestamp = getLong("timestamp") ?: 0L,
        imageUrls = get("imageUrls") as? List<String> ?: emptyList()
    )
}
