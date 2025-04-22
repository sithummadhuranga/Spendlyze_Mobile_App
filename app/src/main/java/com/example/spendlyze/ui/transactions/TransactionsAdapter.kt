package com.example.spendlyze.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendlyze.data.model.Transaction
import com.example.spendlyze.data.model.TransactionType
import com.example.spendlyze.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionsAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemTransactionBinding,
        private val onItemClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(transaction: Transaction) {
            binding.root.setOnClickListener { onItemClick(transaction) }
            binding.textDescription.text = transaction.title
            binding.textCategory.text = transaction.category.name
            binding.textDate.text = dateFormat.format(transaction.date)

            val amountPrefix = if (transaction.type == TransactionType.EXPENSE) "-$" else "+$"
            binding.textAmount.text = "$amountPrefix${String.format("%.2f", transaction.amount)}"
            binding.textAmount.setTextColor(
                binding.root.context.getColor(
                    if (transaction.type == TransactionType.EXPENSE)
                        android.R.color.holo_red_dark
                    else
                        android.R.color.holo_green_dark
                )
            )
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