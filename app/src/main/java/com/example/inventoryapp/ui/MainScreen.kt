package com.example.inventoryapp.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding: PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = "inventory",
            modifier = Modifier.padding(innerPadding)
        ) {
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
            ) { entry ->
                val sale = entry.arguments?.getString("sale") == "true"
                val serial = entry.arguments?.getString("serial").orEmpty()
                val item = entry.arguments?.getString("item").orEmpty()

                TransactionScreen(
                    navController = navController,
                    defaultType = if (sale) "Sale" else "Purchase",
                    defaultSerial = serial,
                    defaultItem = item
                )
            }
            composable("transaction") {
                TransactionScreen(navController)
            }
            composable("scanner") {
                BarcodeScannerScreen { _ ->
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "inventory",
            onClick = { navController.navigate("inventory") },
            icon = {}, label = { Text("Inventory") }
        )
        NavigationBarItem(
            selected = currentRoute == "sold",
            onClick = { navController.navigate("sold") },
            icon = {}, label = { Text("Sold") }
        )
        NavigationBarItem(
            selected = currentRoute == "transactions",
            onClick = { navController.navigate("transactions") },
            icon = {}, label = { Text("Reports") }
        )
    }
}
