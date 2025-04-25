package com.example.spendlyze.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentSettingsBinding
import com.example.spendlyze.utils.BackupManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        setupProfileSection()
        setupClickListeners()
        observeSettingsState()
    }

    private fun setupProfileSection() {
        viewModel.currentUser?.let { user ->
            binding.textUsername.text = user.username
            binding.textEmail.text = user.email
            Glide.with(requireContext())
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.imageProfile)
        }
    }

    private fun setupClickListeners() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTheme(if (isChecked) "Dark" else "Light")
        }
        
        binding.cardCurrency.setOnClickListener {
            showCurrencySelectionDialog()
        }

        binding.budgetCard.setOnClickListener {
            showBudgetDialog()
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
            viewModel.settingsState.collect { state ->
                binding.switchDarkMode.isChecked = state.theme == "Dark"
                binding.textCurrency.text = state.currency
                binding.textBudget.text = String.format("%.2f ${state.currency}", state.monthlyBudget)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
                findNavController().navigate(R.id.action_settings_to_login)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCurrencySelectionDialog() {
        val currencies = arrayOf("LKR", "USD", "EUR", "GBP")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Currency")
            .setItems(currencies) { _, which ->
                viewModel.updateCurrency(currencies[which])
            }
            .show()
    }

    private fun showBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_budget, null)
        AlertDialog.Builder(requireContext())
            .setTitle("Update Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val amount = dialogView.findViewById<android.widget.EditText>(R.id.amountInput)
                    .text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateMonthlyBudget(amount)
            }
            .setNegativeButton("Cancel", null)
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
            .setNegativeButton("Cancel", null)
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
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 