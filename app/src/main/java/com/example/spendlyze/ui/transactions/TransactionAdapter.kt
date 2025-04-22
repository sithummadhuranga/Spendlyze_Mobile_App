package com.example.spendlyze.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendlyze.R
import com.example.spendlyze.databinding.ItemTransactionBinding
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(transaction: Transaction) {
            binding.apply {
                descriptionText.text = transaction.description
                amountText.text = when (transaction.type) {
                    TransactionType.INCOME -> "+${transaction.amount}"
                    TransactionType.EXPENSE -> "-${transaction.amount}"
                }
                amountText.setTextColor(ContextCompat.getColor(root.context, 
                    if (transaction.type == TransactionType.INCOME) R.color.income_green else R.color.expense_red))
                
                categoryText.text = transaction.category
                dateText.text = dateFormatter.format(transaction.date)
                
                typeIcon.setImageResource(
                    if (transaction.type == TransactionType.INCOME) R.drawable.ic_income
                    else R.drawable.ic_expense
                )
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 