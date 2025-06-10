package com.example.inventoryapp.ui.screens

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

@Composable
fun InventoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    var filter by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(Unit) {
        db.collection("transactions")
            .whereEqualTo("type", "Purchase")
            .addSnapshotListener { snapshot, _ ->
                items.clear()
                snapshot?.forEach { doc -> items.add(doc.data + ("id" to doc.id)) }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filteredItems = items.filter {
                val search = filter.lowercase()
                listOf("serial", "model", "phone", "aadhaar").any {
                    (it2 -> (it[it2]?.toString()?.lowercase()?.contains(search) == true))
                }
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
                        Text("Model: ${item["model"]}")
                        Text("Serial: ${item["serial"]}")
                        val imageUrls = item["imageUrls"] as? List<*> ?: emptyList<String>()
                        Row {
                            imageUrls.take(3).forEach {
                                Image(
                                    painter = rememberAsyncImagePainter(it.toString()),
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).padding(4.dp)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                navController.navigate("transaction?serial=${item["serial"]}")
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

    if (selectedItem != null) {
        val item = selectedItem!!
        Dialog(onDismissRequest = { selectedItem = null }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Details for ${item["model"]}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Serial: ${item["serial"]}")
                    Text("Phone: ${item["phone"]}")
                    Text("Aadhaar: ${item["aadhaar"]}")
                    Text("Description: ${item["description"]}")
                    Text("Date: ${item["date"]}")
                    val imageUrls = item["imageUrls"] as? List<*> ?: emptyList<String>()
                    imageUrls.forEach {
                        Image(
                            painter = rememberAsyncImagePainter(it.toString()),
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
