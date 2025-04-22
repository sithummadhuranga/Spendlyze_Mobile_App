package com.example.spendlyze.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.spendlyze.data.model.Transaction
import com.example.spendlyze.data.model.TransactionCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TransactionRepository {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        val transactions: List<Transaction> = gson.fromJson(json, type)
        transactionsFlow.value = transactions
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
        transactionsFlow.value = transactions
    }

    override fun getAllTransactions(): Flow<List<Transaction>> = transactionsFlow

    override fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>> =
        transactionsFlow.map { transactions ->
            transactions.filter { it.category == category }
        }

    override fun getTransactionsByDateRange(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        transactionsFlow.map { transactions ->
            transactions.filter { it.date in startDate..endDate }
        }

    override suspend fun addTransaction(transaction: Transaction) {
        val currentTransactions = transactionsFlow.value.toMutableList()
        currentTransactions.add(transaction)
        saveTransactions(currentTransactions)
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val currentTransactions = transactionsFlow.value.toMutableList()
        val index = currentTransactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            currentTransactions[index] = transaction
            saveTransactions(currentTransactions)
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        val currentTransactions = transactionsFlow.value.toMutableList()
        currentTransactions.removeAll { it.id == transaction.id }
        saveTransactions(currentTransactions)
    }

    override suspend fun getTransactionById(id: String): Transaction? =
        transactionsFlow.value.find { it.id == id }

    override fun getTotalExpenses(): Flow<Double> =
        transactionsFlow.map { transactions ->
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
        }

    override fun getTotalIncome(): Flow<Double> =
        transactionsFlow.map { transactions ->
            transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
        }

    override fun getExpensesByCategory(): Flow<Map<TransactionCategory, Double>> =
        transactionsFlow.map { transactions ->
            transactions
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
        }

    override suspend fun getTotalSpent(): Double {
        return withContext(Dispatchers.IO) {
            val transactions = transactionsFlow.value
            transactions.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
        }
    }

    override suspend fun getTotalIncome(): Double {
        return withContext(Dispatchers.IO) {
            val transactions = transactionsFlow.value
            transactions.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
        }
    }

    override suspend fun getTotalSavings(): Double {
        return withContext(Dispatchers.IO) {
            val transactions = transactionsFlow.value
            transactions.filter { it.type == TransactionType.SAVING }
                .sumOf { it.amount }
        }
    }

    companion object {
        private const val PREF_NAME = "spendlyze_preferences"
        private const val KEY_TRANSACTIONS = "transactions"
    }
} 