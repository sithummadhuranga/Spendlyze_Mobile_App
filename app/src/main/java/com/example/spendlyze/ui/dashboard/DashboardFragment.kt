package com.example.spendlyze.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendlyze.R
import com.example.spendlyze.adapters.TransactionAdapter
import com.example.spendlyze.databinding.FragmentDashboardBinding
import com.example.spendlyze.databinding.DialogUpdateBudgetBinding
import com.example.spendlyze.models.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter
    private var deletedTransaction: Transaction? = null

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
        setupClickListeners()
        observeDashboardState()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onTransactionClick = { transaction ->
                // Handle transaction click
            },
            onTransactionLongClick = { transaction ->
                showDeleteConfirmationDialog(transaction)
            }
        )
        
        binding.recyclerRecentTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            
            // Setup swipe to delete
            val swipeHandler = object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Get the transaction directly from the adapter's current list
                    val position = viewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION && position < transactionAdapter.currentList.size) {
                        val transaction = transactionAdapter.currentList[position]
                        deletedTransaction = transaction
                        viewModel.deleteTransaction(transaction.id)
                        
                        // Show undo snackbar
                        Snackbar.make(
                            binding.root,
                            R.string.transaction_deleted,
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.undo) {
                            deletedTransaction?.let { viewModel.undoDelete(it) }
                        }.show()
                    }
                }
            }
            
            ItemTouchHelper(swipeHandler).attachToRecyclerView(this)
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
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.monthly_budget)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val newBudget = dialogBinding.budgetInput.text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateMonthlyBudget(newBudget)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun observeDashboardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collectLatest { state ->
                binding.apply {
                    textTotalBalance.text = getString(R.string.currency_format, state.totalBalance)
                    textIncome.text = getString(R.string.currency_format, state.totalIncome)
                    textExpenses.text = getString(R.string.currency_format, state.totalExpenses)
                    textMonthlyBudget.text = getString(R.string.currency_format, state.monthlyBudget)
                    progressBudget.progress = ((state.totalExpenses / state.monthlyBudget) * 100).toInt()
                    textSpent.text = getString(R.string.currency_format, state.totalExpenses)
                    textRemaining.text = getString(R.string.currency_format, state.monthlyBudget - state.totalExpenses)
                    transactionAdapter.submitList(state.recentTransactions)
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                // Store the transaction for undo
                deletedTransaction = transaction
                
                // Log the transaction ID for debugging
                android.util.Log.d("DashboardFragment", "Deleting transaction with ID: ${transaction.id}")
                
                // Delete the transaction
                viewModel.deleteTransaction(transaction.id)
                
                // Show undo snackbar
                Snackbar.make(
                    binding.root,
                    R.string.transaction_deleted,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.undo) {
                    deletedTransaction?.let { viewModel.undoDelete(it) }
                }.show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 