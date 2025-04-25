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
import com.example.spendlyze.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeAuthState()
    }

    private fun validateUsername(username: String): Boolean {
        if (username.isBlank()) {
            showError("Username cannot be empty")
            return false
        }
        if (username.length < 3) {
            showError("Username must be at least 3 characters long")
            return false
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            showError("Username can only contain letters, numbers, and underscores")
            return false
        }
        return true
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
        if (!password.matches(Regex(".*[A-Z].*"))) {
            showError("Password must contain at least one uppercase letter")
            return false
        }
        if (!password.matches(Regex(".*[0-9].*"))) {
            showError("Password must contain at least one number")
            return false
        }
        return true
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): Boolean {
        if (confirmPassword.isBlank()) {
            showError("Please confirm your password")
            return false
        }
        if (password != confirmPassword) {
            showError("Passwords do not match")
            return false
        }
        return true
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setupClickListeners() {
        binding.buttonSignUp.setOnClickListener {
            val username = binding.inputUsername.text.toString()
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirmPassword = binding.inputConfirmPassword.text.toString()
            
            if (!validateUsername(username) || 
                !validateEmail(email) || 
                !validatePassword(password) || 
                !validateConfirmPassword(password, confirmPassword)) {
                return@setOnClickListener
            }
            
            viewModel.signUp(username, email, password)
        }
        
        binding.textLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signup_to_login)
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
                    findNavController().navigate(R.id.action_signup_to_dashboard)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 