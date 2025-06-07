package com.example.inventoryapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class InventoryItem(
    val serialNumber: String = "",
    val itemName: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val aadhaarNumber: String = "",
    val isSold: Boolean = false,
    val timestamp: Long = 0L
)

@Composable
fun InventoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var inventory by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            db.collection("inventory")
                .whereEqualTo("isSold", false)
                .addSnapshotListener { snapshot, _ ->
                    val items = snapshot?.documents?.mapNotNull {
                        it.toObject(InventoryItem::class.java)
                    } ?: emptyList()
                    inventory = items
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it.trim() },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn {
            val filtered = inventory.filter {
                it.itemName.contains(searchQuery, ignoreCase = true)
                        || it.customerName.contains(searchQuery, ignoreCase = true)
                        || it.phoneNumber.contains(searchQuery, ignoreCase = true)
                        || it.aadhaarNumber.contains(searchQuery, ignoreCase = true)
                        || it.serialNumber.contains(searchQuery, ignoreCase = true)
            }

            items(filtered.size) { index ->
                val item = filtered[index]
                InventoryCard(item, onSell = {
                    navController.navigate("transaction?sale=true&serial=${item.serialNumber}&item=${item.itemName}")
                })
            }
        }
    }
}

@Composable
fun InventoryCard(item: InventoryItem, onSell: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Item: ${item.itemName}")
            Text("Serial: ${item.serialNumber}")
            Text("Customer: ${item.customerName}")
            Text("Phone: ${item.phoneNumber}")
            Text("Aadhaar: ${item.aadhaarNumber}")

            Spacer(Modifier.height(8.dp))

            Button(onClick = onSell, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("Sell This Item")
            }
        }
    }
}
