package com.example.spendlyze.ui.budget

import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.PreferencesRepository
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetState(
    val monthlyBudget: Double = 0.0,
    val currentSpending: Double = 0.0,
    val spendingPercentage: Int = 0,
    val monthlySpendingData: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class BudgetEvent {
    data class ShowError(val message: String) : BudgetEvent()
    data class ShowBudgetWarning(val message: String) : BudgetEvent()
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val transactionRepository: TransactionRepository
) : BaseViewModel<BudgetState, BudgetEvent>() {

    override fun createInitialState(): BudgetState = BudgetState()

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                combine(
                    preferencesRepository.getMonthlyBudget(),
                    transactionRepository.getTotalExpenses()
                ) { budget, expenses ->
                    val percentage = if (budget > 0) ((expenses / budget) * 100).toInt() else 0
                    BudgetState(
                        monthlyBudget = budget,
                        currentSpending = expenses,
                        spendingPercentage = percentage,
                        monthlySpendingData = calculateMonthlySpending(),
                        isLoading = false
                    )
                }.collect { state ->
                    setState { state }
                    checkBudgetWarning(state)
                }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = e.message) }
                setEvent(BudgetEvent.ShowError(e.message ?: "Failed to load budget data"))
            }
        }
    }

    private suspend fun calculateMonthlySpending(): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val monthlyData = mutableListOf<Pair<String, Double>>()

        // Get last 6 months of spending
        for (i in 5 downTo 0) {
            calendar.set(Calendar.YEAR, currentYear)
            calendar.set(Calendar.MONTH, currentMonth - i)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = calendar.time

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = calendar.time

            val monthSpending = transactionRepository.getTransactionsByDateRange(startDate, endDate)
                .collect { transactions ->
                    monthlyData.add(
                        Pair(
                            calendar.getDisplayName(
                                Calendar.MONTH,
                                Calendar.SHORT,
                                java.util.Locale.getDefault()
                            ),
                            transactions.sumOf { it.amount }
                        )
                    )
                }
        }

        return monthlyData
    }

    private fun checkBudgetWarning(state: BudgetState) {
        if (state.monthlyBudget > 0 && state.spendingPercentage >= 90) {
            setEvent(BudgetEvent.ShowBudgetWarning(
                "You have reached ${state.spendingPercentage}% of your monthly budget!"
            ))
        }
    }

    fun setBudget(amount: String) {
        val budget = amount.toDoubleOrNull()
        if (budget == null || budget <= 0) {
            setEvent(BudgetEvent.ShowError("Please enter a valid budget amount"))
            return
        }

        viewModelScope.launch {
            try {
                preferencesRepository.setMonthlyBudget(budget)
                loadBudgetData()
            } catch (e: Exception) {
                setEvent(BudgetEvent.ShowError(e.message ?: "Failed to set budget"))
            }
        }
    }

    fun refresh() {
        loadBudgetData()
    }
} 