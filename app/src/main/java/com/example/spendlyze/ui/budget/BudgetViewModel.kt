package com.example.spendlyze.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.models.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetState(
    val monthlyBudget: Double = 1000.0, // Default budget amount
    val totalExpenses: Double = 0.0,
    val percentageUsed: Double = 0.0,
    val isLoading: Boolean = false
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
            _budgetState.value = _budgetState.value.copy(isLoading = true)
            
            transactionRepository.getAllTransactions().collect { transactions ->
                val totalExpenses = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                val currentBudget = _budgetState.value.monthlyBudget
                val percentageUsed = if (currentBudget > 0) {
                    (totalExpenses / currentBudget) * 100
                } else {
                    0.0
                }

                _budgetState.value = _budgetState.value.copy(
                    totalExpenses = totalExpenses,
                    percentageUsed = percentageUsed,
                    isLoading = false
                )
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            _budgetState.value = _budgetState.value.copy(
                monthlyBudget = amount,
                percentageUsed = (_budgetState.value.totalExpenses / amount) * 100
            )
        }
    }
} 