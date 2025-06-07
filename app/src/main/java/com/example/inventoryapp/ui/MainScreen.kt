package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Receipt

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(navController, startDestination = "inventory", modifier = Modifier.padding(padding)) {

            composable("inventory") {
                InventoryScreen(navController)
            }

            composable("sold") {
                SoldScreen()
            }

            composable("transactions") {
                TransactionListScreen()
            }

            composable(
                "transaction?sale={sale}&serial={serial}&item={item}",
                arguments = listOf(
                    navArgument("sale") { defaultValue = "false" },
                    navArgument("serial") { defaultValue = "" },
                    navArgument("item") { defaultValue = "" }
                )
            ) { backStackEntry ->
                val sale = backStackEntry.arguments?.getString("sale") == "true"
                val serial = backStackEntry.arguments?.getString("serial") ?: ""
                val item = backStackEntry.arguments?.getString("item") ?: ""
				TransactionScreen(
				navController = navController,
				defaultType = if (sale) "Sale" else "Purchase",
				defaultSerial = serial,
				defaultItem = item
				)
            }

            composable("scanner") {
                BarcodeScannerScreen { scannedValue ->
                    // Navigate back and prefill serial field
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val current = navController.currentBackStackEntryAsState().value?.destination?.route

        NavigationBarItem(
            selected = current == "inventory",
            onClick = { navController.navigate("inventory") },
            label = { Text("Inventory") },
            icon = { Icon(Icons.Default.List, contentDescription = null) }
        )
        NavigationBarItem(
            selected = current == "sold",
            onClick = { navController.navigate("sold") },
            label = { Text("Sold") },
            icon = { Icon(Icons.Default.History, contentDescription = null) }
        )
        NavigationBarItem(
            selected = current == "transactions",
            onClick = { navController.navigate("transactions") },
            label = { Text("Reports") },
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) }
        )
    }
}
