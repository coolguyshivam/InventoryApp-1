package com.example.inventoryapp.model

data class TransactionRecord(
    val transactionType: String = "",
    val serialNumber: String = "",
    val itemName: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val aadhaarNumber: String = "",
    val amount: String = "",
    val date: String = "",
    val timestamp: Long = 0L,
    val imageUrls: List<String> = emptyList()
)
