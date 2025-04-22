package com.example.spendlyze.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spendlyze.R
import com.example.spendlyze.models.Transaction
import com.example.spendlyze.models.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        private val transactionTitle: TextView = itemView.findViewById(R.id.transactionTitle)
        private val transactionDate: TextView = itemView.findViewById(R.id.transactionDate)
        private val transactionAmount: TextView = itemView.findViewById(R.id.transactionAmount)

        fun bind(transaction: Transaction) {
            transactionTitle.text = transaction.title
            transactionDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(transaction.date))
            
            // Format amount with sign and color based on transaction type
            val amount = when (transaction.type) {
                TransactionType.INCOME -> {
                    transactionAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.income_green))
                    "+LKR %.2f"
                }
                TransactionType.EXPENSE -> {
                    transactionAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.expense_red))
                    "-LKR %.2f"
                }
            }.format(transaction.amount)
            
            transactionAmount.text = amount
            
            // Set category icon based on transaction category
            val iconResId = when (transaction.category.lowercase()) {
                "food" -> R.drawable.ic_food
                "transport" -> R.drawable.ic_transport
                "shopping" -> R.drawable.ic_shopping
                "bills" -> R.drawable.ic_bills
                else -> R.drawable.ic_other
            }
            categoryIcon.setImageResource(iconResId)
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