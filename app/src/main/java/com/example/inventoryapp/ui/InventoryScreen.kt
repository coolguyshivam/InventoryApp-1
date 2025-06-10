package com.example.inventoryapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun InventoryScreen(navController: NavHostController) {
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("transactions")
            .whereEqualTo("type", "Purchase")
            .addSnapshotListener { snapshot, _ ->
                items.clear()
                snapshot?.forEach { doc ->
                    items.add(doc.data + ("id" to doc.id))
                }
            }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(items.size) { index ->
            val item = items[index]
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { /* Could show expanded detail */ }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Model: ${item["model"]}")
                    Text("Serial: ${item["serial"]}")
                    Text("Phone: ${item["phone"]}")
                    Text("Aadhaar: ${item["aadhaar"]}")
                    Text("Date: ${item["date"]}")
                    Text("Description: ${item["description"]}")
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            navController.navigate("transaction?serial=${item["serial"]}")
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Sell This Item")
                    }
                }
            }
        }
    }
}
