package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.inventoryapp.ui.InventoryScreen
import com.example.inventoryapp.ui.TransactionScreen
import com.example.inventoryapp.ui.TransactionListScreen

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Inventory", "Transaction", "Reports")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> InventoryScreen(navController = navController)
            1 -> TransactionScreen(navController = navController)
            2 -> TransactionListScreen(navController = navController)
        }
    }
}
