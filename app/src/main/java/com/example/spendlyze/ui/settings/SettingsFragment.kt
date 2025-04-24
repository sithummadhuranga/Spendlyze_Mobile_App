package com.example.spendlyze.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentSettingsBinding
import com.example.spendlyze.utils.BackupManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var backupManager: BackupManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeSettingsState()
    }

    private fun setupClickListeners() {
        binding.budgetCard.setOnClickListener {
            showBudgetDialog()
        }

        binding.currencyCard.setOnClickListener {
            showCurrencyDialog()
        }

        binding.themeCard.setOnClickListener {
            showThemeDialog()
        }

        binding.backupButton.setOnClickListener {
            showBackupDialog()
        }

        binding.restoreButton.setOnClickListener {
            showRestoreDialog()
        }
    }

    private fun observeSettingsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settingsState.observe(viewLifecycleOwner) { state ->
                binding.currentBudgetText.text = viewModel.formatCurrency(state.monthlyBudget)
                binding.currentCurrencyText.text = state.currency
                binding.currentThemeText.text = state.theme
            }
        }
    }

    private fun showBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_budget, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Update Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val amount = dialogView.findViewById<android.widget.EditText>(R.id.amountInput)
                    .text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateMonthlyBudget(amount)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }

    private fun showCurrencyDialog() {
        val currencies = arrayOf("LKR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "INR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, currencies)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Currency")
            .setAdapter(adapter) { _, which ->
                viewModel.updateCurrency(currencies[which])
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("System Default", "Light", "Dark")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, themes)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setAdapter(adapter) { _, which ->
                viewModel.updateTheme(themes[which])
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Backup Data")
            .setMessage("This will create a backup of your transactions.")
            .setPositiveButton("Backup") { _, _ ->
                lifecycleScope.launch {
                    try {
                        backupManager.createBackup()
                        Toast.makeText(requireContext(), "Backup created successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showRestoreDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restore Data")
            .setMessage("This will restore your transactions from the last backup. Current data will be replaced.")
            .setPositiveButton("Restore") { _, _ ->
                lifecycleScope.launch {
                    try {
                        backupManager.restoreBackup()
                        Toast.makeText(requireContext(), "Data restored successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 