package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

data class PurchaseItem(
    val id: String = "",
    val itemName: String = "",
    val serialNumber: String = "",
    val customerName: String = "",
    val timestamp: Long = 0L
)

@Composable
fun InventoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val purchasesRef = db.collection("purchases").orderBy("timestamp", Query.Direction.DESCENDING)
    val salesRef = db.collection("sales")

    var availableItems by remember { mutableStateOf(listOf<PurchaseItem>()) }
    var lastVisible by remember { mutableStateOf<QueryDocumentSnapshot?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Initial load
    LaunchedEffect(Unit) {
        purchasesRef.limit(200).get().addOnSuccessListener { purchaseSnap ->
            salesRef.get().addOnSuccessListener { saleSnap ->
                val soldSerials = saleSnap.documents.mapNotNull { it.getString("serialNumber") }.toSet()
                val filtered = purchaseSnap.documents.filter {
                    val serial = it.getString("serialNumber") ?: ""
                    serial !in soldSerials
                }.map {
                    PurchaseItem(
                        id = it.id,
                        itemName = it.getString("itemName") ?: "",
                        serialNumber = it.getString("serialNumber") ?: "",
                        customerName = it.getString("customerName") ?: "",
                        timestamp = it.getLong("timestamp") ?: 0L
                    )
                }
                availableItems = filtered
                lastVisible = purchaseSnap.documents.lastOrNull() as? QueryDocumentSnapshot
            }
        }
    }

    // Pagination
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isLoading &&
            listState.firstVisibleItemIndex >= availableItems.size - 5 &&
            lastVisible != null
        ) {
            isLoading = true
            purchasesRef.startAfter(lastVisible!!).limit(100).get().addOnSuccessListener { moreSnap ->
                salesRef.get().addOnSuccessListener { saleSnap ->
                    val soldSerials = saleSnap.documents.mapNotNull { it.getString("serialNumber") }.toSet()
                    val filtered = moreSnap.documents.filter {
                        val serial = it.getString("serialNumber") ?: ""
                        serial !in soldSerials
                    }.map {
                        PurchaseItem(
                            id = it.id,
                            itemName = it.getString("itemName") ?: "",
                            serialNumber = it.getString("serialNumber") ?: "",
                            customerName = it.getString("customerName") ?: "",
                            timestamp = it.getLong("timestamp") ?: 0L
                        )
                    }
                    availableItems = availableItems + filtered
                    lastVisible = moreSnap.documents.lastOrNull() as? QueryDocumentSnapshot
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Available Inventory", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(state = listState) {
            items(availableItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Item: ${item.itemName}")
                        Text("Serial: ${item.serialNumber}")
                        Text("Customer: ${item.customerName}")
                        Text("Date: ${java.text.SimpleDateFormat("dd MMM yyyy HH:mm").format(java.util.Date(item.timestamp))}")

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("saleSerial", item.serialNumber)
                            navController.currentBackStackEntry?.savedStateHandle?.set("saleItem", item.itemName)
                            navController.navigate("main") // navigates back to MainScreen
                        }) {
                            Text("Sell This Item")
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
}
