package com.example.spendlyze.ui.analysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
import java.text.NumberFormat

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
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 65f
            transparentCircleRadius = 68f
            setDrawCenterText(true)
            centerText = "Expenses"
            setCenterTextSize(16f)
            setCenterTextColor(Color.BLACK)
            setDrawEntryLabels(false)
            isRotationEnabled = true
            setUsePercentValues(true)
            legend.isEnabled = false
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
                Color.parseColor("#FF6B6B"), // Food
                Color.parseColor("#4ECDC4"), // Transport
                Color.parseColor("#45B7D1"), // Shopping
                Color.parseColor("#96CEB4"), // Bills
                Color.parseColor("#FFEEAD"), // Entertainment
                Color.parseColor("#D4A5A5"), // Health
                Color.parseColor("#9B59B6"), // Education
                Color.parseColor("#95A5A6")  // Other
            )
            valueTextSize = 0f // Hide values on slices
            sliceSpace = 2f // Add space between slices
        }

        binding.pieChart.data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
            setValueTextSize(11f)
            setValueTextColor(Color.BLACK)
        }

        binding.pieChart.invalidate()
    }

    private fun updateLegend(transactions: List<Transaction>) {
        val legendLayout = view?.findViewById<LinearLayout>(R.id.legendLayout)
        legendLayout?.removeAllViews()

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

        // Create legend items for each category
        val categories = resources.getStringArray(R.array.transaction_categories)
        categories.forEachIndexed { index, category ->
            val amount = categoryTotals[category] ?: 0.0
            val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0
            
            val legendItem = LayoutInflater.from(context).inflate(
                R.layout.item_legend,
                legendLayout,
                false
            )

            val colorView = legendItem.findViewById<View>(R.id.colorView)
            val categoryName = legendItem.findViewById<TextView>(R.id.tvCategoryName)
            val categoryAmount = legendItem.findViewById<TextView>(R.id.tvCategoryAmount)

            // Set category color
            colorView.setBackgroundColor(
                when (index) {
                    0 -> Color.parseColor("#FF6B6B") // Food
                    1 -> Color.parseColor("#4ECDC4") // Transport
                    2 -> Color.parseColor("#45B7D1") // Shopping
                    3 -> Color.parseColor("#96CEB4") // Bills
                    4 -> Color.parseColor("#FFEEAD") // Entertainment
                    5 -> Color.parseColor("#D4A5A5") // Health
                    6 -> Color.parseColor("#9B59B6") // Education
                    else -> Color.parseColor("#95A5A6") // Other
                }
            )

            categoryName.text = category
            categoryAmount.text = String.format(
                "%s (%.1f%%)",
                formatCurrency(amount),
                percentage
            )

            legendLayout?.addView(legendItem)
        }

        // Update total expenses with proper currency formatting
        binding.tvTotalExpenses.text = String.format(
            "Total Expenses: %s",
            formatCurrency(totalExpenses)
        )
    }

    private fun formatCurrency(amount: Double): String {
        val currency = viewModel.getCurrency()
        return String.format("%s %.2f", currency, amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 