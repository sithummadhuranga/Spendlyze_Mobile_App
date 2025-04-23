package com.example.spendlyze.adapters

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

class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit,
    private val onTransactionLongClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        android.util.Log.d("TransactionAdapter", "Binding transaction at position $position with ID: ${transaction.id}")
        holder.bind(transaction)
    }
    
    fun getTransactionAt(position: Int): Transaction? {
        return if (position in 0 until currentList.size) {
            currentList[position]
        } else {
            null
        }
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTransactionClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTransactionLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                descriptionText.text = transaction.description
                amountText.text = String.format("LKR %.2f", transaction.amount)
                categoryText.text = transaction.category
                dateText.text = dateFormatter.format(transaction.date)
                amountText.setTextColor(ContextCompat.getColor(root.context, 
                    if (transaction.type == TransactionType.INCOME) R.color.income_green else R.color.expense_red))
                
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