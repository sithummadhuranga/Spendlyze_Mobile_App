package com.example.spendlyze.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class SettingsState(
    val monthlyBudget: Double = 0.0,
    val currency: String = "LKR",
    val theme: String = "System Default"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _settingsState = MutableLiveData<SettingsState>()
    val settingsState: LiveData<SettingsState> = _settingsState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val budget = repository.getMonthlyBudget()
                val currency = repository.getCurrency()
                val theme = repository.getTheme()
                _settingsState.value = SettingsState(budget, currency, theme)
            } catch (e: Exception) {
                // If there's an error, use default values
                _settingsState.value = SettingsState()
            }
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            try {
                repository.updateMonthlyBudget(amount)
                _settingsState.value = _settingsState.value?.copy(monthlyBudget = amount)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            try {
                // Ensure we're using just the currency code
                val currencyCode = currency.split(" - ")[0]
                repository.updateCurrency(currencyCode)
                _settingsState.value = _settingsState.value?.copy(currency = currencyCode)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            try {
                repository.updateTheme(theme)
                _settingsState.value = _settingsState.value?.copy(theme = theme)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.currency = Currency.getInstance(_settingsState.value?.currency ?: "LKR")
        return formatter.format(amount)
    }
} 