package com.example.spendlyze.ui.base

import androidx.lifecycle.ViewModel
import com.example.spendlyze.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor(
    protected val repository: TransactionRepository
) : ViewModel() {
    val currency: StateFlow<String> = repository.currency

    fun formatCurrency(amount: Double): String {
        return String.format("%s %.2f", repository.currency.value, amount)
    }
} 