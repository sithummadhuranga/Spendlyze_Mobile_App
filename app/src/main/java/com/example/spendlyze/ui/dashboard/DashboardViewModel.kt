package com.example.spendlyze.ui.dashboard

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

data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
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

                _dashboardState.value = _dashboardState.value.copy(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    isLoading = false
                )
            }
        }
    }
} 