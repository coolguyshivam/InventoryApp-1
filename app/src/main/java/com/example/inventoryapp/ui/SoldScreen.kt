package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.inventoryapp.model.InventoryItem
import com.example.inventoryapp.ui.components.InventoryCard

@Composable
fun SoldScreen() {
    val soldItems = remember { mutableStateListOf<InventoryItem>() }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("transactions")
            .whereEqualTo("type", "Sale")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                soldItems.clear()
                snapshot?.forEach { doc ->
                    soldItems.add(
                        InventoryItem(
                            id = doc.id,
                            model = doc.getString("model") ?: "",
                            serial = doc.getString("serial") ?: "",
                            phone = doc.getString("phone") ?: "",
                            aadhaar = doc.getString("aadhaar") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getString("date") ?: "",
                            imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList(),
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    )
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Sold Items", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(soldItems.size) { index ->
                InventoryCard(soldItems[index])
            }
        }
    }
}
