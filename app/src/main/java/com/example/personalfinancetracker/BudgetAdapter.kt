package com.example.personalfinancetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class BudgetAdapter(
    private val budgetItems: List<BudgetItem>
) : RecyclerView.Adapter<BudgetAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTxt: TextView = itemView.findViewById(R.id.titleTxt)
        val priceTxt: TextView = itemView.findViewById(R.id.priceTxt)
        val percentTxt: TextView = itemView.findViewById(R.id.percentTxt)
        val circularProgressBar: CircularProgressBar = itemView.findViewById(R.id.circularProgressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_budget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val budgetItem = budgetItems[position]
        holder.titleTxt.text = budgetItem.category
        holder.priceTxt.text = "${budgetItem.amountSpent}/Month"
        holder.percentTxt.text = "${budgetItem.percentage}%"
        holder.circularProgressBar.progress = budgetItem.percentage
    }

    override fun getItemCount(): Int = budgetItems.size
}