package com.example.spendlyze.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.spendlyze.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeDashboardState()
    }

    private fun observeDashboardState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collectLatest { state ->
                updateDashboardUI(state)
            }
        }
    }

    private fun updateDashboardUI(state: DashboardState) {
        binding.apply {
            // Update total balance
            textTotalBalance.text = String.format("LKR %.2f", state.totalBalance)
            
            // Update income
            textIncome.text = String.format("LKR %.2f", state.totalIncome)
            
            // Update expenses
            textExpenses.text = String.format("LKR %.2f", state.totalExpenses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 