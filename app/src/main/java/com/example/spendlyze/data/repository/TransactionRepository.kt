package com.example.spendlyze.data.repository

import com.example.spendlyze.data.model.Transaction
import com.example.spendlyze.data.model.TransactionCategory
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: String): Transaction?
    fun getTotalExpenses(): Flow<Double>
    fun getTotalIncome(): Flow<Double>
    fun getExpensesByCategory(): Flow<Map<TransactionCategory, Double>>
    suspend fun getTotalSpent(): Double
    suspend fun getTotalSavings(): Double
} 