package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.inventoryapp.model.InventoryItem

@Composable
fun InventoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val items = remember { mutableStateListOf<InventoryItem>() }
    var filter by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }

    // Handle scanned serial for filtering or selection
    val navBackStackEntry = navController.currentBackStackEntry
    val scannedSerial = navBackStackEntry?.savedStateHandle?.get<String>("scannedSerial")
    LaunchedEffect(scannedSerial) {
        scannedSerial?.let {
            filter = it
            navBackStackEntry.savedStateHandle.remove<String>("scannedSerial")
        }
    }

    // Realtime listener to fetch only "Purchase" type
    LaunchedEffect(Unit) {
        db.collection("transactions")
            .whereEqualTo("type", "Purchase")
            .addSnapshotListener { snapshot, _ ->
                items.clear()
                snapshot?.forEach { doc ->
                    val data = doc.data
                    val item = InventoryItem(
                        id = doc.id,
                        model = data["model"] as? String ?: "",
                        serial = data["serial"] as? String ?: "",
                        phone = data["phone"] as? String ?: "",
                        aadhaar = data["aadhaar"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        imageUrls = data["imageUrls"] as? List<String> ?: emptyList(),
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                    )
                    items.add(item)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Search Box
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        // Card List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filteredItems = items.filter { item ->
                val searchLower = filter.trim().lowercase()
                searchLower.isBlank() ||
                    item.serial.lowercase().contains(searchLower) ||
                    item.model.lowercase().contains(searchLower) ||
                    item.phone.lowercase().contains(searchLower) ||
                    item.aadhaar.lowercase().contains(searchLower)
            }

            items(filteredItems.size) { index ->
                val item = filteredItems[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { selectedItem = item }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Model: ${item.model}")
                        Text("Serial: ${item.serial}")
                        Row {
                            item.imageUrls.take(3).forEach {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).padding(4.dp)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                navController.navigate("transaction?serial=${item.serial}")
                            },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text("Sell This Item")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Full Detail + Image Preview
    if (selectedItem != null) {
        val item = selectedItem!!
        Dialog(onDismissRequest = { selectedItem = null }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Details for ${item.model}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Serial: ${item.serial}")
                    Text("Phone: ${item.phone}")
                    Text("Aadhaar: ${item.aadhaar}")
                    Text("Description: ${item.description}")
                    Text("Date: ${item.date}")
                    item.imageUrls.forEach {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(vertical = 4.dp)
                        )
                    }
                    Button(
                        onClick = { selectedItem = null },
                        modifier = Modifier.align(Alignment.End).padding(top = 12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}