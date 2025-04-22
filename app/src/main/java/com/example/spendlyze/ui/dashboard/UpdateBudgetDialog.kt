package com.example.spendlyze.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.spendlyze.databinding.DialogUpdateBudgetBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UpdateBudgetDialog : DialogFragment() {
    private var _binding: DialogUpdateBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogUpdateBudgetBinding.inflate(layoutInflater)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Budget")
            .setView(binding.root)
            .setPositiveButton("Update") { _, _ ->
                val budgetText = binding.budgetInput.text.toString()
                if (budgetText.isNotEmpty()) {
                    val budget = budgetText.toDoubleOrNull()
                    if (budget != null && budget >= 0) {
                        viewModel.updateMonthlyBudget(budget)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
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