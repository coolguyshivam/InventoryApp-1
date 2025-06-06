package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
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
    val salesCollection = db.collection("sales").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

    var soldItems by remember { mutableStateOf(listOf<SoldItem>()) }
    var lastVisible by remember { mutableStateOf<QueryDocumentSnapshot?>(null) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    fun loadInitial() {
        salesCollection.limit(200).get().addOnSuccessListener { snapshot ->
            val items = snapshot.documents.mapNotNull { it.toSoldItem() }
            soldItems = items
            lastVisible = snapshot.documents.lastOrNull() as? QueryDocumentSnapshot
        }
    }

    LaunchedEffect(Unit) {
        loadInitial()
    }

    // Pagination
    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        if (!isLoadingMore && listState.firstVisibleItemIndex >= listState.layoutInfo.totalItemsCount - 5 && lastVisible != null) {
            isLoadingMore = true
            salesCollection
                .startAfter(lastVisible!!)
                .limit(100)
                .get()
                .addOnSuccessListener { snapshot ->
                    val moreItems = snapshot.documents.mapNotNull { it.toSoldItem() }
                    soldItems = soldItems + moreItems
                    lastVisible = snapshot.documents.lastOrNull() as? QueryDocumentSnapshot
                    isLoadingMore = false
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.trim() },
                label = { Text("Search by item, customer or serial") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { loadInitial() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(state = listState) {
            val filteredList = soldItems.filter {
                val query = searchQuery.lowercase()
                it.itemName.lowercase().contains(query) ||
                it.customerName.lowercase().contains(query) ||
                it.serialNumber.contains(query)
            }

            items(filteredList) { item ->
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
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(end = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isLoadingMore) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

private fun QueryDocumentSnapshot.toSoldItem(): SoldItem? {
    val serial = getString("serialNumber") ?: return null
    val item = getString("itemName") ?: ""
    val customer = getString("customerName") ?: ""
    val ts = getLong("timestamp") ?: 0L
    val images = get("imageUrls") as? List<String> ?: emptyList()
    return SoldItem(serial, item, customer, ts, images)
}
