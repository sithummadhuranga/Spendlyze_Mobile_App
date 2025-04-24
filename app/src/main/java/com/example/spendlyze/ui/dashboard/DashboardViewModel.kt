package com.example.spendlyze.ui.dashboard

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.example.spendlyze.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val currency: String = "LKR"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: TransactionRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel(repository) {
    
    private val prefs = context.getSharedPreferences("spendlyze_prefs", Context.MODE_PRIVATE)
    private val _dashboardState = MutableStateFlow(DashboardState(
        monthlyBudget = prefs.getFloat("monthly_budget", 1000f).toDouble()
    ))
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        loadDashboardState()
        observeSettings()
    }

    private fun loadDashboardState() {
        viewModelScope.launch {
            combine(
                repository.getAllTransactions(),
                repository.getTotalAmountByType(TransactionType.INCOME),
                repository.getTotalAmountByType(TransactionType.EXPENSE),
                repository.monthlyBudget,
                repository.currency
            ) { transactions, income, expense, budget, currentCurrency ->
                DashboardState(
                    totalBalance = income - expense,
                    totalIncome = income,
                    totalExpense = expense,
                    monthlyBudget = budget,
                    recentTransactions = transactions.take(5),
                    currency = currentCurrency
                )
            }.collect { state ->
                _dashboardState.value = state
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            repository.monthlyBudget.collectLatest { _ ->
                loadDashboardState()
            }
        }
        viewModelScope.launch {
            repository.currency.collectLatest { _ ->
                loadDashboardState()
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            repository.updateMonthlyBudget(amount)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            try {
                val transactions = repository.getAllTransactions().first()
                transactions.find { it.id == transactionId }?.let { transaction ->
                    repository.deleteTransaction(transaction)
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Error deleting transaction: ${e.message}")
            }
        }
    }
    
    fun undoDelete(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
} 