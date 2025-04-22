package com.example.spendlyze.models

import java.util.Date

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Date,
    val type: TransactionType
) 