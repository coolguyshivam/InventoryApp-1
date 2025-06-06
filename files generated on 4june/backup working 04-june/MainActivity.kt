package com.example.inventoryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inventoryapp.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") { MainScreen(navController) }
                composable("scan") {
                    BarcodeScannerScreen { scannedValue ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scannedSerial", scannedValue)
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}