package com.example.spendlyze.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.spendlyze.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    // ViewModel implementation will be added later
} 