package com.example.spendlyze.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.spendlyze.R
import com.example.spendlyze.data.repository.TransactionRepository
import com.example.spendlyze.data.repository.UserRepository
import com.example.spendlyze.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup the bottom navigation with the navigation controller
        binding.navView.setupWithNavController(navController)

        // Hide bottom navigation initially
        binding.navView.visibility = View.GONE

        // Observe navigation changes to show/hide bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_login, R.id.navigation_signup -> {
                    binding.navView.visibility = View.GONE
                }
                else -> {
                    if (userRepository.isLoggedIn()) {
                        binding.navView.visibility = View.VISIBLE
                    } else {
                        binding.navView.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun applyTheme() {
        when (transactionRepository.getTheme()) {
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
} 