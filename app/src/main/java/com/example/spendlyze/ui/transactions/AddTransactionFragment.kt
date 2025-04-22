package com.example.spendlyze.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.spendlyze.R
import com.example.spendlyze.databinding.DialogAddTransactionBinding
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.example.spendlyze.ui.viewmodel.TransactionViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTransactionDialog : DialogFragment() {
    private var _binding: DialogAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionViewModel by viewModels()
    private var selectedDate: Date = Date()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val categories = listOf("Food", "Transport", "Shopping", "Bills", "Other")

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
        setupClickListeners()
    }

    private fun setupViews() {
        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, categories)
        binding.categorySpinner.setAdapter(categoryAdapter)

        // Setup date
        binding.dateInput.setText(dateFormatter.format(selectedDate))
        binding.dateInput.setOnClickListener { showDatePicker() }
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            saveTransaction()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.time
                binding.dateInput.setText(dateFormatter.format(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTransaction() {
        val amount = binding.amountInput.text.toString().toDoubleOrNull()
        val description = binding.descriptionInput.text.toString()
        val category = binding.categorySpinner.text.toString()
        val type = if (binding.typeRadioGroup.checkedRadioButtonId == R.id.radio_income) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }

        if (amount == null || description.isBlank()) {
            Snackbar.make(binding.root, R.string.error_invalid_input, Snackbar.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            amount = amount,
            description = description,
            type = type,
            date = selectedDate,
            category = category
        )

        viewModel.addTransaction(transaction)
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