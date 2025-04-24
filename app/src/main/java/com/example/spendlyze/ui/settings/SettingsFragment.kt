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
        binding.updateBudgetButton.setOnClickListener {
            showUpdateBudgetDialog()
        }

        binding.updateCurrencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        binding.updateThemeButton.setOnClickListener {
            showThemeSelectionDialog()
        }

        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun observeSettingsState() {
        viewModel.settingsState.observe(viewLifecycleOwner) { state ->
            binding.apply {
                currentBudgetText.text = getString(R.string.currency_format, state.monthlyBudget)
                currentCurrencyText.text = state.currency
                currentThemeText.text = state.theme
            }
        }
    }

    private fun showUpdateBudgetDialog() {
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

    private fun showCurrencySelectionDialog() {
        val currencies = arrayOf(
            getString(R.string.currency_lkr),
            getString(R.string.currency_usd),
            getString(R.string.currency_eur),
            getString(R.string.currency_gbp),
            getString(R.string.currency_inr)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_currency)
            .setItems(currencies) { _, which ->
                viewModel.updateCurrency(currencies[which])
            }
            .show()
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_theme)
            .setItems(themes) { _, which ->
                viewModel.updateTheme(themes[which])
            }
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about)
            .setMessage(getString(R.string.version))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 