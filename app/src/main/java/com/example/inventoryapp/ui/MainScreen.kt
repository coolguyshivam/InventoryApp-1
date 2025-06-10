package com.example.inventoryapp.ui

// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Inventory
// import androidx.compose.material.icons.filled.List
// import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.inventoryapp.ui.InventoryScreen
import com.example.inventoryapp.ui.TransactionScreen
import com.example.inventoryapp.ui.TransactionListScreen
import com.example.inventoryapp.model.TransactionRecord

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("Inventory", Icons.Filled.Inventory),
        TabItem("Transaction", Icons.Filled.ShoppingCart),
        TabItem("Reports", Icons.Filled.List)
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
    ) { innerPadding ->
        when (selectedTab) {
            0 -> InventoryScreen(navController)
            1 -> TransactionScreen(navController)
            2 -> TransactionListScreen()
        }
    }
}

data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
