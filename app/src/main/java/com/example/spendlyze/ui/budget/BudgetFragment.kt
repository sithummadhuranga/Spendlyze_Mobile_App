package com.example.spendlyze.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendlyze.adapters.TransactionAdapter
import com.example.spendlyze.databinding.FragmentBudgetBinding
import com.example.spendlyze.models.TransactionType
import com.example.spendlyze.databinding.DialogUpdateBudgetBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

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
        setupRecyclerView()
        observeBudgetState()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.recentExpensesRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeBudgetState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.budgetState.collectLatest { state ->
                updateBudgetUI(state)
            }
        }
    }

    private fun updateBudgetUI(state: BudgetState) {
        binding.apply {
            budgetAmount.text = String.format("$%.2f", state.monthlyBudget)
            totalExpensesText.text = String.format("$%.2f spent", state.totalExpenses)
            budgetPercentageText.text = String.format("%.1f%% Used", state.percentageUsed)
            budgetProgressBar.progress = state.percentageUsed.toInt()
            
            // Update progress bar color based on percentage
            budgetProgressBar.setIndicatorColor(
                when {
                    state.percentageUsed >= 90 -> 0xFFFF5252.toInt() // Red
                    state.percentageUsed >= 75 -> 0xFFFFB74D.toInt() // Orange
                    else -> 0xFF4CAF50.toInt() // Green
                }
            )
        }
    }

    private fun setupClickListeners() {
        binding.budgetCard.setOnClickListener {
            showUpdateBudgetDialog()
        }
    }

    private fun showUpdateBudgetDialog() {
        val dialogBinding = DialogUpdateBudgetBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { dialog, _ ->
                val amount = dialogBinding.budgetAmountInput.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.updateMonthlyBudget(amount)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 