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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
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
            val monthlyBudget = transactionRepository.getMonthlyBudget()
            
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                val totalIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val totalExpenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                val balance = totalIncome - totalExpenses
                
                _dashboardState.value = DashboardState(
                    totalBalance = balance,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    monthlyBudget = monthlyBudget,
                    recentTransactions = transactions.take(5)
                )
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            transactionRepository.updateMonthlyBudget(amount)
            loadDashboardData()
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            // Get the current list of transactions
            val currentTransactions = transactionRepository.getAllTransactions().first()
            
            // Log the transaction ID and current transactions for debugging
            android.util.Log.d("DashboardViewModel", "Attempting to delete transaction with ID: $transactionId")
            android.util.Log.d("DashboardViewModel", "Current transactions: ${currentTransactions.map { it.id }}")
            
            // Find the transaction to delete by ID
            val transactionToDelete = currentTransactions.find { it.id == transactionId }
            
            // Log if transaction was found
            if (transactionToDelete != null) {
                android.util.Log.d("DashboardViewModel", "Found transaction to delete: ${transactionToDelete.id}")
                transactionRepository.deleteTransaction(transactionToDelete)
            } else {
                android.util.Log.e("DashboardViewModel", "Transaction with ID $transactionId not found")
            }
        }
    }
    
    fun undoDelete(transaction: Transaction) {
        viewModelScope.launch {
            // Insert the transaction back
            transactionRepository.insertTransaction(transaction)
        }
    }
} 