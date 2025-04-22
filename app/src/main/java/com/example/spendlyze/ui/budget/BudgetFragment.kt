package com.example.spendlyze.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendlyze.R
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
        transactionAdapter = TransactionAdapter(
            onTransactionClick = { transaction ->
                // Navigate to transaction details or edit screen
                // TODO: Implement transaction details navigation
            },
            onTransactionLongClick = { transaction ->
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(transaction.id)
            }
        )
        binding.recentExpensesRecyclerView.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showDeleteConfirmationDialog(transactionId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTransaction(transactionId)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            // Update budget title
            budgetTitle.text = "Monthly Budget"
            
            // Update budget amount
            budgetAmount.text = String.format("LKR %.2f", state.monthlyBudget)
            
            // Update budget percentage
            budgetPercentageText.text = String.format("%.1f%%", state.budgetPercentage)
            
            // Update total expenses
            totalExpensesText.text = String.format("LKR %.2f", state.totalExpenses)
            
            // Update progress indicator
            budgetProgressBar.progress = state.budgetPercentage.toInt()
            
            // Update recent expenses
            transactionAdapter.submitList(state.recentExpenses)
            
            // Show/hide empty state
            if (state.recentExpenses.isEmpty()) {
                recentExpensesRecyclerView.visibility = View.GONE
                recentExpensesTitle.visibility = View.GONE
            } else {
                recentExpensesRecyclerView.visibility = View.VISIBLE
                recentExpensesTitle.visibility = View.VISIBLE
            }
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
                val amount = dialogBinding.budgetInput.text.toString().toDoubleOrNull()
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