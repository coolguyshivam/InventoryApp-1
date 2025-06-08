package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun SoldScreen() {
    val db = FirebaseFirestore.getInstance()
    var soldItems by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val result = db.collection("inventory")
            .whereEqualTo("isSold", true)
            .get()
            .await()

        soldItems = result.documents.mapNotNull { it.toObject(InventoryItem::class.java) }
            .sortedByDescending { it.timestamp }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn {
            items(soldItems.size) { index ->
                val item = soldItems[index]
                InventoryCard(item = item, onSell = {})
            }
        }
    }
}
