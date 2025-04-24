package com.example.spendlyze.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentSettingsBinding
import com.example.spendlyze.databinding.DialogUpdateBudgetBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatDelegate
import java.util.Currency

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

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

        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun observeSettingsState() {
        viewModel.settingsState.observe(viewLifecycleOwner) { state ->
            binding.currentBudgetText.text = viewModel.formatCurrency(state.monthlyBudget)
            binding.currentCurrencyText.text = state.currency
            binding.currentThemeText.text = state.theme
        }
    }

    private fun showBudgetDialog() {
        val dialogBinding = DialogUpdateBudgetBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { dialog, _ ->
                val amount = dialogBinding.budgetInput.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.updateMonthlyBudget(amount)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showCurrencyDialog() {
        val currencies = Currency.getAvailableCurrencies().sortedBy { it.currencyCode }
        val currencyItems = currencies.map { "${it.currencyCode} - ${it.displayName}" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Currency")
            .setItems(currencyItems) { _, which ->
                val selectedCurrency = currencies.elementAt(which).currencyCode
                viewModel.updateCurrency(selectedCurrency)
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("System Default", "Light", "Dark")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Theme")
            .setItems(themes) { _, which ->
                val selectedTheme = themes[which]
                viewModel.updateTheme(selectedTheme)
                applyTheme(selectedTheme)
            }
            .show()
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Spendlyze")
            .setMessage("Version 1.0.0\n\nSpendlyze is a personal finance management app that helps you track your expenses and manage your budget effectively.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 