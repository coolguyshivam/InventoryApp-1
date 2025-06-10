package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun TransactionListScreen(navController: NavHostController) {
    val transactions = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                transactions.clear()
                snapshot?.forEach { doc ->
                    val type = doc.getString("type") ?: "Transaction"
                    val model = doc.getString("model") ?: ""
                    val serial = doc.getString("serial") ?: ""
                    val date = doc.getString("date") ?: ""
                    transactions.add("$type - $model - $serial - $date")
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("All Transactions", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(transactions.size) { index ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(transactions[index], modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
