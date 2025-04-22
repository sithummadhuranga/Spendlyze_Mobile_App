package com.example.spendlyze.models

import java.util.Date

data class Transaction(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long,
    val type: TransactionType
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 