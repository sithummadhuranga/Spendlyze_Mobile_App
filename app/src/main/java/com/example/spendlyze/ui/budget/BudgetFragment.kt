package com.example.spendlyze.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentBudgetBinding
import com.example.spendlyze.ui.viewmodel.TransactionViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                updateBudgetProgress(transactions)
            }
        }
    }

    private fun setupClickListeners() {
        binding.setBudgetButton.setOnClickListener {
            showSetBudgetDialog()
        }
    }

    private fun updateBudgetProgress(transactions: List<Transaction>) {
        // Update budget progress UI
        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val budget = 50000.0 // This should come from BudgetManager
        val progress = (totalExpense / budget * 100).toInt()
        
        binding.budgetProgressBar.progress = progress.coerceIn(0, 100)
        binding.spentAmountText.text = getString(R.string.currency_format, totalExpense)
        binding.remainingAmountText.text = getString(R.string.currency_format, budget - totalExpense)
        
        // Update progress color based on percentage
        val colorResId = when {
            progress >= 100 -> R.color.expense_red
            progress >= 90 -> R.color.category_bills
            else -> R.color.income_green
        }
        
        binding.budgetProgressBar.setProgressTintList(
            android.content.res.ColorStateList.valueOf(
                requireContext().getColor(colorResId)
            )
        )
    }

    private fun showSetBudgetDialog() {
        // Show dialog to set budget
        // This will be implemented later
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 