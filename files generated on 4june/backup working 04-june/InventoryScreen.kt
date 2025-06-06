package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

data class PurchaseItem(
    val id: String,
    val itemName: String,
    val customerName: String,
    val serialNumber: String,
    val timestamp: Long
)

@Composable
fun InventoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val listState = rememberLazyListState()

    var availableItems by remember { mutableStateOf(listOf<PurchaseItem>()) }
    var lastVisible by remember { mutableStateOf<QueryDocumentSnapshot?>(null) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    fun loadInitial() {
        db.collection("purchases")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(200)
            .get()
            .addOnSuccessListener { purchasesSnap ->
                db.collection("sales").get().addOnSuccessListener { salesSnap ->
                    val soldSerials = salesSnap.documents.mapNotNull { it["serialNumber"] as? String }.toSet()
                    val filtered = purchasesSnap.documents.filter {
                        val serial = it.getString("serialNumber") ?: ""
                        serial !in soldSerials
                    }.map { doc ->
                        PurchaseItem(
                            id = doc.id,
                            itemName = doc.getString("itemName") ?: "",
                            customerName = doc.getString("customerName") ?: "",
                            serialNumber = doc.getString("serialNumber") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                    availableItems = filtered
                    lastVisible = purchasesSnap.documents.lastOrNull() as? QueryDocumentSnapshot
                }
            }
    }

    LaunchedEffect(Unit) {
        loadInitial()
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        val nearBottom = listState.firstVisibleItemIndex >= listState.layoutInfo.totalItemsCount - 5
        if (nearBottom && !isLoadingMore && lastVisible != null) {
            isLoadingMore = true
            db.collection("purchases")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible!!)
                .limit(100)
                .get()
                .addOnSuccessListener { purchasesSnap ->
                    db.collection("sales").get().addOnSuccessListener { salesSnap ->
                        val soldSerials = salesSnap.documents.mapNotNull { it["serialNumber"] as? String }.toSet()
                        val filtered = purchasesSnap.documents.filter {
                            val serial = it.getString("serialNumber") ?: ""
                            serial !in soldSerials
                        }.map { doc ->
                            PurchaseItem(
                                id = doc.id,
                                itemName = doc.getString("itemName") ?: "",
                                customerName = doc.getString("customerName") ?: "",
                                serialNumber = doc.getString("serialNumber") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )
                        }
                        availableItems = availableItems + filtered
                        lastVisible = purchasesSnap.documents.lastOrNull() as? QueryDocumentSnapshot
                        isLoadingMore = false
                    }
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.trim() },
                label = { Text("Search") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { loadInitial() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(state = listState) {
            val filteredList = availableItems.filter {
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
                        Text("Date: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm").format(java.util.Date(item.timestamp))}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            navController.navigate("main") {
                                launchSingleTop = true
                                restoreState = true
                            }
                            navController.currentBackStackEntry?.savedStateHandle?.set("saleSerial", item.serialNumber)
                            navController.currentBackStackEntry?.savedStateHandle?.set("saleItem", item.itemName)
                        }) {
                            Text("Sell This Item")
                        }
                    }
                }
            }
        }
    }
}
