package com.example.spendlyze.ui.budget

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentBudgetBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        setupBudgetInput()
        observeViewModel()
    }

    private fun setupChart() {
        binding.spendingChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
            legend.isEnabled = true
            animateX(1000)
        }
    }

    private fun setupBudgetInput() {
        binding.budgetInput.setOnEditorActionListener { _, _, _ ->
            val budgetText = binding.budgetInput.text.toString()
            if (budgetText.isNotEmpty()) {
                val budget = budgetText.toDoubleOrNull()
                if (budget != null) {
                    viewModel.setBudget(budget)
                }
            }
            true
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.budgetState.collect { state ->
                updateChart(state.spendingData)
                updateBudgetProgress(state.budget, state.totalSpent)
            }
        }
    }

    private fun updateChart(spendingData: List<Pair<String, Double>>) {
        val entries = spendingData.mapIndexed { index, (_, amount) ->
            Entry(index.toFloat(), amount.toFloat())
        }

        val dataSet = LineDataSet(entries, "Spending").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        binding.spendingChart.apply {
            data = LineData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(spendingData.map { it.first })
            invalidate()
        }
    }

    private fun updateBudgetProgress(budget: Double, spent: Double) {
        val progress = ((spent / budget) * 100).toInt().coerceIn(0, 100)
        binding.budgetProgress.apply {
            setProgress(progress)
            setIndicatorColor(
                when {
                    progress >= 90 -> requireContext().getColor(R.color.danger)
                    progress >= 75 -> requireContext().getColor(R.color.warning)
                    else -> requireContext().getColor(R.color.success)
                }
            )
        }

        binding.budgetStatus.text = getString(
            R.string.budget_status,
            spent,
            budget,
            progress
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 