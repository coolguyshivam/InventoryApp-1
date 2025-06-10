package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun TransactionListScreen(navController: NavHostController) {
    val transactions = remember {
        listOf(
            "Purchase - Model X - IMEI12345 - 2025-06-09",
            "Sale - Model Y - IMEI54321 - 2025-06-08"
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("All Transactions", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(transactions.size) { index ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(
                        text = transactions[index],
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
