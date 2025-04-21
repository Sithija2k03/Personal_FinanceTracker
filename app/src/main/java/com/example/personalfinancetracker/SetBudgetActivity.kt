package com.example.personalfinancetracker

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.databinding.ActivitySetBudgetBinding

class SetBudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetBudgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySetBudgetBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Set up the Toolbar
            binding.toolbar?.let {
                setSupportActionBar(it)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)
            } ?: run {
                Log.e("SetBudgetActivity", "Toolbar is null")
            }

            // Set up the Spinner with currencies
            val currencies = arrayOf("LKR", "USD", "EUR", "GBP")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spCurrency.adapter = adapter

            // Load existing budget
            val budget = DataManager.getBudget(this)
            if (budget != null) {
                binding.etBudgetAmount.setText(budget.monthlyBudget.toString())
                val currencyPosition = currencies.indexOf(budget.currency)
                if (currencyPosition != -1) {
                    binding.spCurrency.setSelection(currencyPosition)
                } else {
                    binding.spCurrency.setSelection(0) // Default to LKR if currency not found
                }
            } else {
                binding.spCurrency.setSelection(0) // Default to LKR if no budget
            }

            binding.btnSaveBudget.setOnClickListener {
                saveBudget()
            }
        } catch (e: Exception) {
            Log.e("SetBudgetActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error starting Set Budget: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun saveBudget() {
        val budgetStr = binding.etBudgetAmount.text.toString()
        val currency = binding.spCurrency.selectedItem.toString()

        if (budgetStr.isEmpty()) {
            Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetAmount = budgetStr.toDoubleOrNull()
        if (budgetAmount == null || budgetAmount <= 0) {
            Toast.makeText(this, "Invalid budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        val budget = Budget(budgetAmount, currency)
        DataManager.saveBudget(this, budget)
        Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}