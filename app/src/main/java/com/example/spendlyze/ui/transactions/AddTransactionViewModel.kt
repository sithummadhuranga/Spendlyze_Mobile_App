package com.example.spendlyze.ui.transactions

import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.model.Transaction
import com.example.spendlyze.data.model.TransactionCategory
import com.example.spendlyze.data.model.TransactionType
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class AddTransactionState(
    val title: String = "",
    val amount: String = "",
    val category: TransactionCategory? = null,
    val date: Date = Date(),
    val type: TransactionType = TransactionType.EXPENSE,
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false
)

sealed class AddTransactionEvent {
    data class ShowError(val message: String) : AddTransactionEvent()
    object NavigateBack : AddTransactionEvent()
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : BaseViewModel<AddTransactionState, AddTransactionEvent>() {

    override fun createInitialState(): AddTransactionState = AddTransactionState()

    fun onTitleChanged(title: String) {
        setState {
            copy(
                title = title,
                titleError = if (title.isBlank()) "Title cannot be empty" else null
            ).validateForm()
        }
    }

    fun onAmountChanged(amount: String) {
        setState {
            copy(
                amount = amount,
                amountError = when {
                    amount.isBlank() -> "Amount cannot be empty"
                    amount.toDoubleOrNull() == null -> "Invalid amount"
                    amount.toDouble() <= 0 -> "Amount must be positive"
                    else -> null
                }
            ).validateForm()
        }
    }

    fun onCategoryChanged(category: TransactionCategory) {
        setState {
            copy(
                category = category,
                categoryError = null
            ).validateForm()
        }
    }

    fun onDateChanged(date: Date) {
        setState { copy(date = date).validateForm() }
    }

    fun onTypeChanged(type: TransactionType) {
        setState { copy(type = type).validateForm() }
    }

    private fun AddTransactionState.validateForm(): AddTransactionState {
        val isSaveEnabled = title.isNotBlank() &&
                amount.isNotBlank() &&
                amount.toDoubleOrNull() != null &&
                amount.toDouble() > 0 &&
                category != null &&
                titleError == null &&
                amountError == null &&
                categoryError == null

        return copy(isSaveEnabled = isSaveEnabled)
    }

    fun onSaveClicked() {
        val currentState = currentState
        if (!currentState.isSaveEnabled) return

        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                val transaction = Transaction(
                    title = currentState.title,
                    amount = currentState.amount.toDouble(),
                    category = currentState.category!!,
                    date = currentState.date,
                    type = currentState.type
                )
                transactionRepository.addTransaction(transaction)
                setEvent(AddTransactionEvent.NavigateBack)
            } catch (e: Exception) {
                setEvent(AddTransactionEvent.ShowError(e.message ?: "Failed to save transaction"))
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
} 