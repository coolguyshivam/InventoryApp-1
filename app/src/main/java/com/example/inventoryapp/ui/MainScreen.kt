package com.example.inventoryapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventoryapp.ui.InventoryScreen
import com.example.inventoryapp.ui.TransactionScreen
import com.example.inventoryapp.ui.TransactionListScreen
import com.example.inventoryapp.ui.BarcodeScannerScreen

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabTitles = listOf("Inventory", "Add Transaction", "Scan IMEI", "Reports")
	val tabIcons = listOf(Icons.Default.Store, Icons.Default.Add, Icons.Default.QrCode, Icons.Default.List)

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabTitles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(tabIcons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> InventoryScreen(navController)
                1 -> TransactionScreen(navController)
                2 -> BarcodeScannerScreen(navController)
                3 -> TransactionListScreen()
            }
        }
    }
}
