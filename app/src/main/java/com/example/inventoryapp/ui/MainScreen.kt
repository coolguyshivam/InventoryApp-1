package com.example.inventoryapp.ui

//import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Inventory
//import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.ui.unit.dp
import com.example.inventoryapp.ui.InventoryScreen
import com.example.inventoryapp.ui.TransactionScreen
import com.example.inventoryapp.ui.TransactionListScreen

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Inventory", "Transaction", "Reports")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> InventoryScreen()
            1 -> TransactionScreen()
            2 -> TransactionListScreen()
        }
    }
}