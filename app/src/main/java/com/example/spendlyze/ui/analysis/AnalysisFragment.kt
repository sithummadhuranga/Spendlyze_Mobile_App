package com.example.spendlyze.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.spendlyze.R
import com.example.spendlyze.databinding.FragmentAnalysisBinding
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import dagger.hilt.android.AndroidEntryPoint

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
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            if (transactions.isEmpty()) {
                showEmptyState()
            } else {
                updatePieChart(transactions)
                updateLegend(transactions)
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
                Color.parseColor("#9C27B0")  // Other
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
        val categoryTotals = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val totalExpenses = categoryTotals.values.sum()
        if (totalExpenses == 0.0) return

        binding.apply {
            tvFoodLegend.text = getLegendText("Food", categoryTotals["Food"] ?: 0.0, totalExpenses)
            tvTransportLegend.text = getLegendText("Transport", categoryTotals["Transport"] ?: 0.0, totalExpenses)
            tvShoppingLegend.text = getLegendText("Shopping", categoryTotals["Shopping"] ?: 0.0, totalExpenses)
            tvBillsLegend.text = getLegendText("Bills", categoryTotals["Bills"] ?: 0.0, totalExpenses)
            tvOtherLegend.text = getLegendText("Other", categoryTotals["Other"] ?: 0.0, totalExpenses)
        }
    }

    private fun getLegendText(category: String, amount: Double, total: Double): String {
        val percentage = (amount / total * 100).toInt()
        return "$category: $${String.format("%.2f", amount)} ($percentage%)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 