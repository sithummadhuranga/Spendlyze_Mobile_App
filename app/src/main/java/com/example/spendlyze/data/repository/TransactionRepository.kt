package com.example.spendlyze.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class TransactionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private var nextId: Long = 1

    // Settings StateFlows
    private val _monthlyBudget = MutableStateFlow(0.0)
    private val _currency = MutableStateFlow("LKR")
    private val _theme = MutableStateFlow("System Default")

    val monthlyBudget = _monthlyBudget.asStateFlow()
    val currency = _currency.asStateFlow()
    val theme = _theme.asStateFlow()

    init {
        _transactions.value = getTransactionsFromPrefs()
        // Initialize nextId based on existing transactions
        nextId = (_transactions.value.maxOfOrNull { it.id } ?: 0) + 1
        
        // Initialize settings from SharedPreferences
        _monthlyBudget.value = prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        _currency.value = prefs.getString(KEY_CURRENCY, "LKR") ?: "LKR"
        _theme.value = prefs.getString(KEY_THEME, "System Default") ?: "System Default"
    }

    fun getAllTransactions(): Flow<List<Transaction>> = _transactions.asStateFlow()

    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        _transactions.map { transactions ->
            transactions.filter { it.type == type }
        }

    suspend fun insertTransaction(transaction: Transaction) {
        val currentList = _transactions.value.toMutableList()
        // Create a new transaction with a unique ID
        val newTransaction = transaction.copy(id = nextId++)
        currentList.add(newTransaction)
        _transactions.value = currentList
        saveTransactionsToPrefs(currentList)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val currentList = _transactions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            currentList[index] = transaction
            _transactions.value = currentList
            saveTransactionsToPrefs(currentList)
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val currentList = _transactions.value.toMutableList()
        currentList.removeIf { it.id == transaction.id }
        _transactions.value = currentList
        saveTransactionsToPrefs(currentList)
    }

    suspend fun deleteAllTransactions() {
        _transactions.value = emptyList()
        saveTransactionsToPrefs(emptyList())
        nextId = 1
    }

    fun getTotalAmountByType(type: TransactionType): Flow<Double> =
        _transactions.map { transactions ->
            transactions
                .filter { it.type == type }
                .sumOf { it.amount }
        }

    fun updateMonthlyBudget(amount: Double) {
        prefs.edit()
            .putFloat(KEY_MONTHLY_BUDGET, amount.toFloat())
            .apply()
        _monthlyBudget.value = amount
    }

    fun getMonthlyBudget(): Double {
        return _monthlyBudget.value
    }

    fun getCurrency(): String {
        return _currency.value
    }

    fun updateCurrency(currency: String) {
        prefs.edit()
            .putString(KEY_CURRENCY, currency)
            .apply()
        _currency.value = currency
    }

    fun getTheme(): String {
        return _theme.value
    }

    fun updateTheme(theme: String) {
        prefs.edit()
            .putString(KEY_THEME, theme)
            .apply()
        _theme.value = theme
    }

    private fun getTransactionsFromPrefs(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveTransactionsToPrefs(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "transaction_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_THEME = "theme"
    }
} 