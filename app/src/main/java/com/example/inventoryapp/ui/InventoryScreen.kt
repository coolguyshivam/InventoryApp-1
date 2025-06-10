package com.example.inventoryapp.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
	Text("Inventory Screen")
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.trim() },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                val filtered = inventory.filter {
                    it.itemName.contains(searchQuery, ignoreCase = true)
                            || it.customerName.contains(searchQuery, ignoreCase = true)
                            || it.phoneNumber.contains(searchQuery, ignoreCase = true)
                            || it.aadhaarNumber.contains(searchQuery, ignoreCase = true)
                            || it.serialNumber.contains(searchQuery, ignoreCase = true)
                }

                items(filtered.size) { index ->
                    val item = filtered[index]
                    InventoryCard(item = item, onSell = {
                        navController.navigate(
                            "transaction?sale=true&serial=${item.serialNumber}&item=${item.itemName}"
                        )
                    })
                }
            }
        }

        // ✅ FABs
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("transaction") },
                content = { Text("Add Transaction") }
            )
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("scanner") },
                content = { Text("Scan IMEI") }
            )
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

            Button(onClick = onSell) {
                Text("Sell This Item")
            }
        }
    }
}
