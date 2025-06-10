package com.example.inventoryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.inventoryapp.ui.MainScreen
import com.example.inventoryapp.ui.theme.InventoryAppTheme
import com.example.inventoryapp.ui.InventoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InventoryAppTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}