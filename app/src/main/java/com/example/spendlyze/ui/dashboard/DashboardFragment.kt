package com.example.spendlyze.ui.dashboard

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
import com.example.spendlyze.databinding.DialogUpdateBudgetBinding
import com.example.spendlyze.databinding.FragmentDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeDashboardState()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.recyclerRecentTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.cardMonthlyBudget.setOnClickListener {
            showUpdateBudgetDialog()
        }
        
        binding.textViewAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_transactions)
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

    private fun observeDashboardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collectLatest { state ->
                updateDashboardUI(state)
            }
        }
    }

    private fun updateDashboardUI(state: DashboardState) {
        binding.apply {
            // Update total balance
            textTotalBalance.text = String.format("LKR %.2f", state.totalBalance)
            
            // Update income
            textIncome.text = String.format("LKR %.2f", state.totalIncome)
            
            // Update expenses
            textExpenses.text = String.format("LKR %.2f", state.totalExpenses)
            
            // Update monthly budget
            textMonthlyBudget.text = String.format("LKR %.2f", state.monthlyBudget)
            
            // Update budget progress
            progressBudget.progress = state.budgetPercentage.toInt()
            
            // Update spent and remaining amounts
            textSpent.text = String.format("LKR %.2f", state.totalExpenses)
            textRemaining.text = String.format("LKR %.2f", state.monthlyBudget - state.totalExpenses)
            
            // Update recent transactions
            transactionAdapter.submitList(state.recentTransactions)
            
            // Show/hide empty state
            if (state.recentTransactions.isEmpty()) {
                recyclerRecentTransactions.visibility = View.GONE
                recentTransactionsHeader.visibility = View.GONE
            } else {
                recyclerRecentTransactions.visibility = View.VISIBLE
                recentTransactionsHeader.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 