package com.example.spendlyze.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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
class AddTransactionFragment : DialogFragment() {
    private var _binding: DialogAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionViewModel by viewModels()
    private var selectedDate = Calendar.getInstance()
    private var selectedCategory = "Food"

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
        binding.saveButton.setOnClickListener { saveTransaction() }
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
        
        // Set initial selection
        binding.categorySpinner.setText(categories[0], false)
        selectedCategory = categories[0]
        
        binding.categorySpinner.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = categories[position]
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

    private fun validateAmount(amount: Double?): Boolean {
        if (amount == null) {
            showError("Please enter a valid amount")
            return false
        }
        if (amount <= 0) {
            showError("Amount must be greater than 0")
            return false
        }
        return true
    }

    private fun validateDescription(description: String): Boolean {
        if (description.isEmpty()) {
            showError("Description cannot be empty")
            return false
        }
        if (description.length < 3) {
            showError("Description must be at least 3 characters long")
            return false
        }
        return true
    }

    private fun validateDate(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        if (date.after(today)) {
            showError("Cannot add transactions for future dates")
            return false
        }
        return true
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun saveTransaction() {
        val amount = binding.amountInput.text.toString().toDoubleOrNull()
        val description = binding.descriptionInput.text.toString()
        val category = selectedCategory
        val dateStr = binding.dateInput.text.toString()

        // Validate all fields
        if (!validateAmount(amount) || 
            !validateDescription(description) || 
            !validateDate(selectedDate)) {
            return
        }

        if (dateStr.isEmpty()) {
            showError("Please select a date")
            return
        }

        val type = if (binding.typeRadioGroup.checkedRadioButtonId == R.id.radio_income) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        val transaction = Transaction(
            id = 0, // The repository will assign a proper ID
            amount = amount!!,
            description = description,
            category = category,
            date = selectedDate.time,
            type = type
        )

        viewModel.addTransaction(transaction) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddTransactionDialog"
    }
} 