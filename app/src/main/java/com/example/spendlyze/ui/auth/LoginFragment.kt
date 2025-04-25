package com.example.spendlyze.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeAuthState()
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            showError("Email cannot be empty")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address")
            return false
        }
        return true
    }

    private fun validatePassword(password: String): Boolean {
        if (password.isBlank()) {
            showError("Password cannot be empty")
            return false
        }
        if (password.length < 6) {
            showError("Password must be at least 6 characters long")
            return false
        }
        return true
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            
            if (!validateEmail(email) || !validatePassword(password)) {
                return@setOnClickListener
            }
            
            viewModel.login(email, password)
        }
        
        binding.textSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
                }
                
                if (state.isLoggedIn) {
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 