package com.example.spendlyze.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Date,
    val type: TransactionType
) : Parcelable 