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
import kotlinx.coroutines.flow.combine
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
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            transactionRepository.monthlyBudget.collectLatest { budget ->
                loadBudgetData()
            }
        }
        viewModelScope.launch {
            transactionRepository.currency.collectLatest { currency ->
                loadBudgetData()
            }
        }
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactions(),
                transactionRepository.monthlyBudget,
                transactionRepository.currency
            ) { transactions: List<Transaction>, budget: Double, currency: String ->
                val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
                val totalExpense = expenseTransactions.sumOf { it.amount }
                val monthlyBudget = transactionRepository.getMonthlyBudget()
                val remainingBudget = monthlyBudget - totalExpense
                val progress = if (monthlyBudget > 0) (totalExpense / monthlyBudget) * 100 else 0.0

                BudgetState(
                    monthlyBudget = monthlyBudget,
                    totalExpenses = totalExpense,
                    budgetPercentage = progress,
                    recentExpenses = expenseTransactions.take(5)
                )
            }.collect { state ->
                _budgetState.value = state
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