package com.example.inventoryapp.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun InventoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    var inventory by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("transactions")
            .whereEqualTo("type", "Purchase")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    inventory = it.documents.mapNotNull { doc ->
                        doc.data?.plus("id" to doc.id)
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search by model, serial, phone, Aadhaar") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filtered = inventory.filter {
                val q = search.trim().lowercase()
                listOf(it["model"], it["serial"], it["phone"], it["aadhaar"]).any { field ->
                    field?.toString()?.lowercase()?.contains(q) == true
                }
            }

            items(filtered) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            item["serial"]?.let {
                                navController.navigate("transaction?serial=$it")
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Model: ${item["model"]}", style = MaterialTheme.typography.titleMedium)
                        Text("Serial: ${item["serial"]}")
                        Text("Phone: ${item["phone"]}")
                        Text("Aadhaar: ${item["aadhaar"]}")
                        Text("Amount: ₹${item["amount"]}")
                        Text("Date: ${item["date"]}")

                        // Optional preview of attached photos
                        val imageUrls = item["imageUrls"] as? List<*> ?: emptyList<String>()
                        if (imageUrls.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                imageUrls.take(3).forEach { url ->
                                    Image(
                                        painter = rememberAsyncImagePainter(url.toString()),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                item["serial"]?.let {
                                    navController.navigate("transaction?serial=$it&type=Sale")
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Sell This Item")
                        }
                    }
                }
            }
        }
    }
}
