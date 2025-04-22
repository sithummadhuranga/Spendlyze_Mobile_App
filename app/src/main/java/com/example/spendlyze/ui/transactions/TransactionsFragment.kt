package com.example.spendlyze.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentTransactionsBinding
import com.example.spendlyze.data.model.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionsAdapter

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
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TransactionsAdapter(
            onItemClick = { transaction -> showTransactionDetails(transaction) }
        )
        binding.recyclerViewTransactions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TransactionsFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collect { transactions ->
                adapter.submitList(transactions)
                updateEmptyState(transactions.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showTransactionDetails(transaction: Transaction) {
        // TODO: Implement transaction details dialog
    }

    private fun showDeleteConfirmation(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTransaction(transaction)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddTransactionDialog() {
        // TODO: Implement add transaction dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 