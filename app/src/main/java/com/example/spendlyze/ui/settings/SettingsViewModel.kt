package com.example.spendlyze.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.ui.base.BaseViewModel
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
    repository: TransactionRepository
) : BaseViewModel(repository) {

    private val _settingsState = MutableLiveData<SettingsState>()
    val settingsState: LiveData<SettingsState> = _settingsState

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val monthlyBudget = repository.getMonthlyBudget()
            val currency = repository.getCurrency()
            val theme = repository.getTheme()
            _settingsState.value = SettingsState(monthlyBudget, currency, theme)
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
                repository.updateCurrency(currency)
                _settingsState.value = _settingsState.value?.copy(currency = currency)
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
} 