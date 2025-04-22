package com.example.spendlyze.ui.transactions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.spendlyze.R
import com.example.spendlyze.data.model.TransactionCategory
import com.example.spendlyze.data.model.TransactionType
import com.example.spendlyze.databinding.FragmentAddTransactionBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTransactionViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInputs()
        setupCategorySpinner()
        setupDatePicker()
        setupTypeRadioGroup()
        setupSaveButton()
        observeState()
        observeEvents()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupInputs() {
        binding.editTitle.doAfterTextChanged {
            viewModel.onTitleChanged(it?.toString() ?: "")
        }

        binding.editAmount.doAfterTextChanged {
            viewModel.onAmountChanged(it?.toString() ?: "")
        }
    }

    private fun setupCategorySpinner() {
        val categories = TransactionCategory.values()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories.map { it.name }
        )
        binding.spinnerCategory.setAdapter(adapter)
        binding.spinnerCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.onCategoryChanged(categories[position])
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        binding.editDate.setText(dateFormat.format(calendar.time))
        viewModel.onDateChanged(calendar.time)

        binding.editDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.editDate.setText(dateFormat.format(calendar.time))
                    viewModel.onDateChanged(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTypeRadioGroup() {
        binding.groupType.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.radio_expense -> TransactionType.EXPENSE
                R.id.radio_income -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }
            viewModel.onTypeChanged(type)
        }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            viewModel.onSaveClicked()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    binding.layoutTitle.error = state.titleError
                    binding.layoutAmount.error = state.amountError
                    binding.layoutCategory.error = state.categoryError
                    binding.buttonSave.isEnabled = state.isSaveEnabled
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is AddTransactionEvent.ShowError -> showError(event.message)
                        AddTransactionEvent.NavigateBack -> findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 