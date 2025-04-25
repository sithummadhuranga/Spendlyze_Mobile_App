package com.example.spendlyze.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.spendlyze.R
import com.example.spendlyze.databinding.DialogUpdateBudgetBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class UpdateBudgetDialog : DialogFragment() {
    private var _binding: DialogUpdateBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogUpdateBudgetBinding.inflate(layoutInflater)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.monthly_budget)
            .setView(binding.root)
            .setPositiveButton(R.string.update) { dialog, _ ->
                val budgetText = binding.amountInput.text.toString()
                if (budgetText.isNotEmpty()) {
                    val budget = budgetText.toDoubleOrNull()
                    if (budget != null && budget >= 0) {
                        viewModel.updateMonthlyBudget(budget)
                        dialog.dismiss()
                    } else {
                        Snackbar.make(binding.root, R.string.error_invalid_input, Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(binding.root, R.string.error_invalid_input, Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UpdateBudgetDialog"
    }
} 