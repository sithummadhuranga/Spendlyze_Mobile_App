package com.example.spendlyze.models

import java.util.Date

data class Transaction(
    val id: String = "",
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date = Date(),
    val type: TransactionType
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 