package com.example.spendlyze.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(application)
    
    val transactions = repository.getAllTransactions()
    
    val incomeTransactions = repository.getTransactionsByType(TransactionType.INCOME)
    val expenseTransactions = repository.getTransactionsByType(TransactionType.EXPENSE)
    
    val totalIncome = repository.getTotalAmountByType(TransactionType.INCOME)
    val totalExpense = repository.getTotalAmountByType(TransactionType.EXPENSE)
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                val newTransaction = transaction.copy(id = System.currentTimeMillis())
                repository.insertTransaction(newTransaction)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add transaction"
            }
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update transaction"
            }
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete transaction"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 