package com.example.spendlyze.data

import android.content.Context
import android.content.SharedPreferences
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.example.spendlyze.utils.NotificationHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val notificationHelper = NotificationHelper(context)
    
    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()
    
    init {
        _monthlyBudget.value = prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    }
    
    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
    }
    
    fun getSpentAmount(transactions: List<Transaction>): Double {
        return transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
    
    fun getRemainingAmount(transactions: List<Transaction>): Double {
        val spent = getSpentAmount(transactions)
        return _monthlyBudget.value - spent
    }
    
    fun checkBudgetAlert(transactions: List<Transaction>) {
        val spent = getSpentAmount(transactions)
        val budget = _monthlyBudget.value
        
        if (budget <= 0) return
        
        val percentage = (spent / budget) * 100
        
        when {
            percentage >= 100 -> {
                notificationHelper.showBudgetAlertNotification(
                    "Budget Exceeded",
                    "You have exceeded your monthly budget of LKR ${budget.toInt()}"
                )
            }
            percentage >= 90 -> {
                notificationHelper.showBudgetAlertNotification(
                    "Budget Warning",
                    "You have spent ${percentage.toInt()}% of your monthly budget"
                )
            }
        }
    }
    
    fun getTransactions(context: Context): List<Transaction> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    companion object {
        private const val PREFS_NAME = "budget_prefs"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_TRANSACTIONS = "transactions"
    }
} 