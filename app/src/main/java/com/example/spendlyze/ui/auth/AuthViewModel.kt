package com.example.spendlyze.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendlyze.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authState.value = AuthState(isLoggedIn = userRepository.isLoggedIn())
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                if (userRepository.login(email, password)) {
                    _authState.value = AuthState(isLoggedIn = true)
                } else {
                    _authState.value = AuthState(error = "Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message ?: "Login failed")
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                if (userRepository.signUp(username, email, password)) {
                    _authState.value = AuthState(isLoggedIn = true)
                } else {
                    _authState.value = AuthState(error = "Sign up failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState(error = e.message ?: "Sign up failed")
            }
        }
    }

    fun logout() {
        userRepository.logout()
        _authState.value = AuthState(isLoggedIn = false)
    }
} 