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
import com.example.spendlyze.data.repository.UserRepository
import com.example.spendlyze.databinding.FragmentSettingsBinding
import com.example.spendlyze.utils.BackupManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var userRepository: UserRepository

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
        val currentUser = userRepository.getCurrentUser()
        currentUser?.let { user ->
            binding.textUsername.text = user.username
            binding.textEmail.text = user.email
            Glide.with(this)
                .load(user.profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(binding.imageProfile)
        }
    }

    private fun setupClickListeners() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTheme(if (isChecked) "dark" else "light")
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
                binding.switchDarkMode.isChecked = state.isDarkMode
                binding.textCurrency.text = state.currency
                binding.textBudget.text = String.format("%.2f ${state.currency}", state.monthlyBudget)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                userRepository.logout()
                findNavController().navigate(R.id.action_settings_to_login)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCurrencySelectionDialog() {
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY", "INR", "LKR")
        val currentCurrency = viewModel.getCurrency()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_currency)
            .setSingleChoiceItems(currencies, currencies.indexOf(currentCurrency)) { dialog, which ->
                viewModel.setCurrency(currencies[which])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_budget, null)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.update_budget)
            .setView(dialogView)
            .setPositiveButton(R.string.update) { _, _ ->
                val amount = dialogView.findViewById<android.widget.EditText>(R.id.amountInput)
                    .text.toString().toDoubleOrNull() ?: 0.0
                viewModel.updateMonthlyBudget(amount)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.backup)
            .setMessage(R.string.backup_restore_description)
            .setPositiveButton(R.string.backup) { _, _ ->
                lifecycleScope.launch {
                    try {
                        backupManager.createBackup()
                        Toast.makeText(requireContext(), R.string.backup_success, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), R.string.backup_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showRestoreDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.restore)
            .setMessage(R.string.restore_confirmation)
            .setPositiveButton(R.string.restore) { _, _ ->
                lifecycleScope.launch {
                    try {
                        backupManager.restoreBackup()
                        Toast.makeText(requireContext(), R.string.restore_success, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), R.string.restore_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 