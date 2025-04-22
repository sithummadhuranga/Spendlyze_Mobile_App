package com.example.spendlyze.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val monthlyBudget: Double = 1000.0, // Default budget
    val budgetPercentage: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("spendlyze_prefs", Context.MODE_PRIVATE)
    private val _dashboardState = MutableStateFlow(DashboardState(
        monthlyBudget = prefs.getFloat("monthly_budget", 1000f).toDouble()
    ))
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            
            repository.getAllTransactions().collect { transactions ->
                val totalIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                
                val totalExpenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                val totalBalance = totalIncome - totalExpenses
                
                // Calculate budget percentage
                val currentBudget = _dashboardState.value.monthlyBudget
                val budgetPercentage = if (currentBudget > 0) {
                    (totalExpenses / currentBudget) * 100
                } else {
                    0.0
                }
                
                // Get recent transactions (last 5)
                val recentTransactions = transactions
                    .sortedByDescending { it.date }
                    .take(5)

                _dashboardState.value = _dashboardState.value.copy(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    budgetPercentage = budgetPercentage,
                    recentTransactions = recentTransactions,
                    isLoading = false
                )
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            val currentState = _dashboardState.value
            val budgetPercentage = if (amount > 0) {
                (currentState.totalExpenses / amount) * 100
            } else {
                0.0
            }
            
            // Save to SharedPreferences
            prefs.edit()
                .putFloat("monthly_budget", amount.toFloat())
                .apply()
            
            _dashboardState.value = currentState.copy(
                monthlyBudget = amount,
                budgetPercentage = budgetPercentage
            )
        }
    }
} 