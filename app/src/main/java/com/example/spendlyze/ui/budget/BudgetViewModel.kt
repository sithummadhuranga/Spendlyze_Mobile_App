package com.example.spendlyze.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetState(
    val monthlyBudget: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val budgetPercentage: Double = 0.0,
    val recentExpenses: List<Transaction> = emptyList()
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _budgetState = MutableStateFlow(BudgetState())
    val budgetState: StateFlow<BudgetState> = _budgetState.asStateFlow()

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            val monthlyBudget = transactionRepository.getMonthlyBudget()
            
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val totalExpenses = expenses.sumOf { it.amount }
                val budgetPercentage = if (monthlyBudget > 0) {
                    (totalExpenses / monthlyBudget) * 100
                } else {
                    0.0
                }
                
                _budgetState.value = BudgetState(
                    monthlyBudget = monthlyBudget,
                    totalExpenses = totalExpenses,
                    budgetPercentage = budgetPercentage,
                    recentExpenses = expenses.take(5)
                )
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            transactionRepository.updateMonthlyBudget(amount)
            loadBudgetData()
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                val transaction = transactions.find { it.id == transactionId }
                transaction?.let {
                    transactionRepository.deleteTransaction(it)
                    loadBudgetData()
                }
            }
        }
    }
} 