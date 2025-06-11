package com.example.inventoryapp.model

data class Transaction(
    val type: String,
    val model: String,
    val serial: String,
    val phone: String?,
    val aadhaar: String?,
    val amount: Double,
    val date: String,
    val quantity: Int,
    val description: String?
)