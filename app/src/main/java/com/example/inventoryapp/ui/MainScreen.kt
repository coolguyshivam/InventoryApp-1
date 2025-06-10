package com.example.inventoryapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home // Placeholder for Inventory if needed
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.List // Use AutoMirrored version for RTL support
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("Inventory", Icons.Filled.Home),
        TabItem("Transaction", Icons.Filled.ShoppingCart),
        TabItem("Reports", Icons.AutoMirrored.Filled.List) // Updated icon
    )

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        text = { Text(tab.label) }
                    )
                }
            }
        }
    ) { _ -> // Renamed unused parameter to _
        when (selectedTab) {
            0 -> InventoryScreen(navController)
            1 -> TransactionScreen(navController)
            2 -> Text("Reports Tab - Under Construction")
        }
    }
}

data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)