package com.example.spendlyze.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentAnalysisBinding
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
        observeTransactions()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            legend.isEnabled = false
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            setHoleRadius(58f)
            setTransparentCircleRadius(61f)
        }
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                if (transactions.isEmpty()) {
                    showEmptyState()
                } else {
                    updatePieChart(transactions)
                    updateLegend(transactions)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.pieChart.visibility = View.GONE
        binding.legendContainer.visibility = View.GONE
        binding.tvNoData.visibility = View.VISIBLE
    }

    private fun updatePieChart(transactions: List<Transaction>) {
        binding.pieChart.visibility = View.VISIBLE
        binding.legendContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE

        val categoryTotals = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val totalExpenses = categoryTotals.values.sum()
        if (totalExpenses == 0.0) {
            showEmptyState()
            return
        }

        val entries = categoryTotals.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            colors = listOf(
                Color.parseColor("#FF9800"), // Food
                Color.parseColor("#2196F3"), // Transport
                Color.parseColor("#E91E63"), // Shopping
                Color.parseColor("#4CAF50"), // Bills
                Color.parseColor("#9C27B0"), // Entertainment
                Color.parseColor("#FF5722"), // Health
                Color.parseColor("#607D8B"), // Education
                Color.parseColor("#795548")  // Other
            )
        }

        binding.pieChart.data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.BLACK)
        }

        binding.pieChart.invalidate()
    }

    private fun updateLegend(transactions: List<Transaction>) {
        val categoryTotals = mutableMapOf<String, Double>()
        var totalExpenses = 0.0

        // Calculate totals for each category
        transactions.forEach { transaction ->
            if (transaction.type == TransactionType.EXPENSE) {
                val amount = transaction.amount.toDouble()
                categoryTotals[transaction.category] = (categoryTotals[transaction.category] ?: 0.0) + amount
                totalExpenses += amount
            }
        }

        // Update legend text for each category
        val categories = resources.getStringArray(R.array.transaction_categories)
        categories.forEachIndexed { index, category ->
            val amount = categoryTotals[category] ?: 0.0
            val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0
            val legendText = String.format("%s: $%.2f (%.1f%%)", category, amount, percentage)
            
            when (index) {
                0 -> binding.tvFoodLegend.text = legendText
                1 -> binding.tvTransportLegend.text = legendText
                2 -> binding.tvShoppingLegend.text = legendText
                3 -> binding.tvBillsLegend.text = legendText
                4 -> binding.tvEntertainmentLegend.text = legendText
                5 -> binding.tvHealthLegend.text = legendText
                6 -> binding.tvEducationLegend.text = legendText
                7 -> binding.tvOtherLegend.text = legendText
            }
        }

        // Update pie chart
        val entries = categoryTotals.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        val dataSet = PieDataSet(entries, "Expenses by Category")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.category_food),
            ContextCompat.getColor(requireContext(), R.color.category_transport),
            ContextCompat.getColor(requireContext(), R.color.category_shopping),
            ContextCompat.getColor(requireContext(), R.color.category_bills),
            ContextCompat.getColor(requireContext(), R.color.category_entertainment),
            ContextCompat.getColor(requireContext(), R.color.category_health),
            ContextCompat.getColor(requireContext(), R.color.category_education),
            ContextCompat.getColor(requireContext(), R.color.category_other)
        )

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 