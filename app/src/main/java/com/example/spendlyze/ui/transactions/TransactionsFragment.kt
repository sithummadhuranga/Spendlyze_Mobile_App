package com.example.spendlyze.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spendlyze.R
import com.example.spendlyze.adapters.TransactionAdapter
import com.example.spendlyze.databinding.FragmentTransactionsBinding
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.example.spendlyze.ui.transactions.TransactionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment() {
    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter
    private var deletedTransaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onTransactionClick = { transaction ->
                showEditTransactionDialog(transaction)
            },
            onTransactionLongClick = { transaction ->
                showDeleteConfirmationDialog(transaction)
            }
        )
        binding.recyclerView.apply {
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
                    val position = viewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION && position < transactionAdapter.currentList.size) {
                        val transaction = transactionAdapter.currentList[position]
                        
                        // Store for undo
                        deletedTransaction = transaction
                        
                        // Delete the transaction
                        viewModel.deleteTransaction(transaction)
                        
                        // Show undo snackbar
                        Snackbar.make(
                            binding.root,
                            R.string.transaction_deleted,
                            Snackbar.LENGTH_LONG
                        ).setAction(R.string.undo) {
                            deletedTransaction?.let { viewModel.addTransaction(it) }
                        }.show()
                    }
                }
            }
            
            // Attach the ItemTouchHelper to the RecyclerView
            ItemTouchHelper(swipeHandler).attachToRecyclerView(this)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                transactionAdapter.submitList(transactions)
                binding.emptyStateView.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalIncome.collectLatest { income ->
                binding.totalIncomeText.text = viewModel.formatCurrency(income)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalExpense.collectLatest { expense ->
                binding.totalExpenseText.text = viewModel.formatCurrency(expense)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun showAddTransactionDialog() {
        AddTransactionFragment().show(
            childFragmentManager,
            AddTransactionFragment.TAG
        )
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        EditTransactionFragment.newInstance(transaction).show(
            childFragmentManager,
            EditTransactionFragment.TAG
        )
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                // Store for undo
                deletedTransaction = transaction
                
                // Delete the transaction
                viewModel.deleteTransaction(transaction)
                
                // Show undo snackbar
                Snackbar.make(
                    binding.root,
                    R.string.transaction_deleted,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.undo) {
                    deletedTransaction?.let { viewModel.addTransaction(it) }
                }.show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TransactionsFragment"
    }
} 