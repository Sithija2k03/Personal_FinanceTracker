package com.example.personalfinancetracker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,  // Change to MutableList
    private val onDeleteClick: (Transaction) -> Unit,
    private val onEditClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTitle.text = transaction.title
        holder.tvAmount.text = "${if (transaction.isIncome) "+" else "-"} ${transaction.amount}"
        holder.tvCategory.text = transaction.category
        holder.tvDate.text = transaction.date
        holder.btnEdit.setOnClickListener {
            onEditClick(transaction)
        }
        holder.itemView.setOnLongClickListener {
            val index = transactions.indexOf(transaction)
            if (index != -1) {
                transactions.removeAt(index)
                notifyItemRemoved(index)  // Notify specific item removal
                onDeleteClick(transaction)
            }
            true
        }
    }

    override fun getItemCount(): Int = transactions.size

    // Method to update the list and notify changes
    @SuppressLint("NotifyDataSetChanged")
    fun updateTransactions(newTransactions: List<Transaction>) {
        val oldSize = transactions.size
        transactions.clear()
        transactions.addAll(newTransactions)
        if (oldSize == 0 && newTransactions.isNotEmpty()) {
            notifyItemRangeInserted(0, newTransactions.size)
        } else if (newTransactions.isEmpty()) {
            notifyItemRangeRemoved(0, oldSize)
        } else {
            notifyDataSetChanged()  // Fallback for complex changes
        }
    }
}