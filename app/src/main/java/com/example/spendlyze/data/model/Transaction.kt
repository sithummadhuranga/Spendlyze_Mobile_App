package com.example.spendlyze.data.model

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val date: Date = Date(),
    val description: String = ""
) 