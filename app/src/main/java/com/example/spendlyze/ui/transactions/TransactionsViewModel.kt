package com.example.spendlyze.ui.transactions

import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.model.Transaction
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TransactionsEvent {
    data class ShowError(val message: String) : TransactionsEvent()
    data class NavigateToEditTransaction(val transaction: Transaction) : TransactionsEvent()
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel<TransactionsState, TransactionsEvent>() {

    override fun createInitialState(): TransactionsState = TransactionsState()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            transactionRepository.getAllTransactions()
                .catch { e ->
                    setState {
                        copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                    setEvent(TransactionsEvent.ShowError(e.message ?: "Failed to load transactions"))
                }
                .collect { transactions ->
                    setState {
                        copy(
                            transactions = transactions.sortedByDescending { it.date },
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun onTransactionClick(transaction: Transaction) {
        setEvent(TransactionsEvent.NavigateToEditTransaction(transaction))
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
                loadTransactions()
            } catch (e: Exception) {
                setEvent(TransactionsEvent.ShowError(e.message ?: "Failed to delete transaction"))
            }
        }
    }
} 