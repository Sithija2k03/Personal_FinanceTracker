package com.example.personalfinancetracker

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinancetracker.databinding.ActivitySpendingAnalysisBinding

class SpendingAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpendingAnalysisBinding
    private lateinit var budgetAdapter: BudgetAdapter
    private var transactions = mutableListOf<Transaction>()
    private var budget: Budget? = null

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySpendingAnalysisBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Load data
            budget = DataManager.getBudget(this)
            transactions = DataManager.getTransactions(this).toMutableList()

            // Set up back button
            binding.backBtn.setOnClickListener {
                finish()
            }

            // Calculate summary
            updateSummary()

            // Set up RecyclerView with budget items
            setupRecyclerView()
        } catch (e: Exception) {
            Log.e("SpendingAnalysisActivity", "Error in onCreate", e)
            finish()
        }
    }

    private fun updateSummary() {
        val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }
        val totalExpenses = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val totalBalance = totalIncome - totalExpenses
        val savings = totalBalance // Simplified: savings = balance

        val currency = budget?.currency ?: "LKR"
        val budgetAmount = budget?.monthlyBudget ?: 0.0
        val budgetProgress = if (budgetAmount > 0) {
            (totalExpenses / budgetAmount * 100).coerceAtMost(100.0).toFloat()
        } else {
            0f
        }

        // Update CircularProgressBar and budget text
        binding.circularProgressBar.progress = budgetProgress
        binding.textView12.text = "Budget: $currency ${"%.0f".format(totalExpenses)} / ${"%.0f".format(budgetAmount)}"

        // Update summary cards (Income and Expenses)
        binding.textView15.text = "$currency ${"%.2f".format(totalIncome)}"
        binding.textView17.text = "$currency ${"%.2f".format(totalExpenses)}"

        // Update summary row (Total Balance, Income, Savings)
        binding.textView15.text = "$currency ${"%.2f".format(totalBalance)}" // Total Balance
        binding.textView17.text = "$currency ${"%.2f".format(totalIncome)}" // Income
        binding.textView19.text = "$currency ${"%.0f".format(savings)}" // Savings

        // Update percentage changes (placeholder values for now)
        binding.textView15.text = "+25%" // Total Balance change
        binding.textView17.text = "-10%" // Income change
        binding.textView20.text = "+15%" // Savings change
    }

    private fun setupRecyclerView() {
        // Group expenses by category and calculate spending per category
        val budgetItems = transactions
            .filter { !it.isIncome }
            .groupBy { it.category }
            .map { (category, categoryTransactions) ->
                val totalSpent = categoryTransactions.sumOf { it.amount }
                val budgetAmount = budget?.monthlyBudget ?: 0.0
                val percentage = if (budgetAmount > 0) {
                    (totalSpent / budgetAmount * 100).coerceAtMost(100.0).toFloat()
                } else {
                    0f
                }
                BudgetItem(
                    category = category,
                    amountSpent = totalSpent,
                    percentage = percentage
                )
            }

        budgetAdapter = BudgetAdapter(budgetItems)
        binding.view2.layoutManager = LinearLayoutManager(this)
        binding.view2.adapter = budgetAdapter
    }
}