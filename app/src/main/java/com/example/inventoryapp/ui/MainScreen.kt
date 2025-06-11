package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.inventoryapp.ui.*

@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    val items = listOf("Inventory", "Add", "Scan", "Reports")
    val icons = listOf(Icons.Default.Inventory, Icons.Default.Add, Icons.Default.QrCodeScanner, Icons.Default.List)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = currentBackStackEntryAsState(navController).value?.destination?.route
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = screen) },
                        label = { Text(screen) },
                        selected = currentRoute?.startsWith(screen.lowercase()) == true,
                        onClick = {
                            when (screen) {
                                "Inventory" -> navController.navigate("inventory")
                                "Add" -> navController.navigate("transaction")
                                "Scan" -> navController.navigate("scanner")
                                "Reports" -> navController.navigate("reports")
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "inventory",
            modifier = Modifier.padding(padding)
        ) {
            composable("inventory") { InventoryScreen(navController) }

            composable(
                "transaction?serial={serial}&type={type}",
                arguments = listOf(
                    navArgument("serial") { defaultValue = "" },
                    navArgument("type") { defaultValue = "Purchase" }
                )
            ) {
                TransactionScreen(navController)
            }

            composable("transaction") {
                TransactionScreen(navController)
            }

            composable("scanner") {
                BarcodeScannerScreen(navController)
            }

            composable("reports") {
                TransactionListScreen()
            }
        }
    }
}
