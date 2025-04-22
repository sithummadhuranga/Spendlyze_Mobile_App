package com.example.spendlyze.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.spendlyze.R
import com.example.spendlyze.databinding.DialogAddTransactionBinding
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class EditTransactionFragment : DialogFragment() {
    private var _binding: DialogAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private var selectedDate = Calendar.getInstance()
    private var selectedCategory = ""
    private var transaction: Transaction? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadTransactionData()
        binding.saveButton.setOnClickListener { updateTransaction() }
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun setupViews() {
        // Setup category spinner
        setupSpinner()

        // Setup date picker
        binding.dateInput.setOnClickListener { showDatePicker() }
    }

    private fun setupSpinner() {
        val categories = resources.getStringArray(R.array.transaction_categories)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        binding.categorySpinner.setAdapter(adapter)
        
        binding.categorySpinner.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
        }
    }

    private fun loadTransactionData() {
        transaction = arguments?.getParcelable("transaction")
        transaction?.let { transaction ->
            binding.amountInput.setText(transaction.amount.toString())
            binding.descriptionInput.setText(transaction.description)
            binding.categorySpinner.setText(transaction.category, false)
            selectedCategory = transaction.category
            
            selectedDate.time = transaction.date
            binding.dateInput.setText(String.format("%d/%d/%d", 
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedDate.get(Calendar.YEAR)
            ))

            if (transaction.type == TransactionType.INCOME) {
                binding.radioIncome.isChecked = true
            } else {
                binding.radioExpense.isChecked = true
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                binding.dateInput.setText(String.format("%d/%d/%d", month + 1, day, year))
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateTransaction() {
        val amount = binding.amountInput.text.toString().toDoubleOrNull()
        val description = binding.descriptionInput.text.toString()
        val category = selectedCategory
        val dateStr = binding.dateInput.text.toString()

        if (amount == null || description.isEmpty() || dateStr.isEmpty()) {
            Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        val type = if (binding.typeRadioGroup.checkedRadioButtonId == R.id.radio_income) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        transaction?.let { originalTransaction ->
            val updatedTransaction = originalTransaction.copy(
                amount = amount,
                description = description,
                category = category,
                date = selectedDate.time,
                type = type
            )
            viewModel.updateTransaction(updatedTransaction)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditTransactionDialog"

        fun newInstance(transaction: Transaction): EditTransactionFragment {
            return EditTransactionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("transaction", transaction)
                }
            }
        }
    }
} 