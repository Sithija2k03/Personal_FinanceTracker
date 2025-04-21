package com.example.personalfinancetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.databinding.ActivityAddTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("EditTransactionActivity", "onCreate called")
            // Wrap layout inflation and initial setup in a try-catch to catch early crashes
            try {
                binding = ActivityAddTransactionBinding.inflate(layoutInflater)
                setContentView(binding.root)
            } catch (e: Exception) {
                Log.e("EditTransactionActivity", "Error inflating layout", e)
                Toast.makeText(this, "Error loading UI: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Set up the Toolbar
            binding.toolbar?.let {
                setSupportActionBar(it)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)
                supportActionBar?.title = "Edit Transaction"
            } ?: run {
                Log.e("EditTransactionActivity", "Toolbar is null")
                Toast.makeText(this, "Toolbar not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Set up the Spinner with categories
            val categories = arrayOf("Food", "Dining", "Entertainment", "Bills", "Transport", "Personal")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spCategory.adapter = adapter

            // Show DatePickerDialog when the date field is clicked
            binding.etDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val selectedDate = String.format(
                            Locale.US,
                            "%d-%02d-%02d",
                            selectedYear,
                            selectedMonth + 1,
                            selectedDay
                        )
                        binding.etDate.setText(selectedDate)
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }

            // Retrieve the transaction to edit from DataManager
            val transaction = DataManager.getEditingTransaction()
            val transactionIndex = DataManager.getEditingTransactionIndex()
            Log.d("EditTransactionActivity", "Retrieved transaction: $transaction, index: $transactionIndex")
            if (transaction == null || transactionIndex == -1) {
                Log.e("EditTransactionActivity", "Invalid transaction data: transaction=$transaction, index=$transactionIndex")
                Toast.makeText(this, "Invalid transaction data", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Pre-fill the UI with the transaction's data
            binding.etTitle.setText(transaction.title)
            binding.etAmount.setText(transaction.amount.toString())
            val categoryIndex = categories.indexOf(transaction.category)
            if (categoryIndex >= 0) {
                binding.spCategory.setSelection(categoryIndex)
            } else {
                Log.w("EditTransactionActivity", "Invalid category: ${transaction.category}, defaulting to first")
                binding.spCategory.setSelection(0)
            }
            binding.etDate.setText(transaction.date)
            binding.rgType.check(if (transaction.isIncome) R.id.rbIncome else R.id.rbExpense)
            binding.btnSave.text = "Update Transaction"

            // Set up the Save button
            binding.btnSave.setOnClickListener {
                val title = binding.etTitle.text.toString()
                val amountText = binding.etAmount.text.toString()
                val category = binding.spCategory.selectedItem.toString()
                val date = binding.etDate.text.toString()
                val isIncome = binding.rgType.checkedRadioButtonId == R.id.rbIncome

                if (title.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val amount = amountText.toDoubleOrNull()
                if (amount == null) {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate date format (YYYY-MM-DD)
                val dateFormatValidator = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormatValidator.isLenient = false
                try {
                    dateFormatValidator.parse(date)
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid date format (use YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update the transaction in DataManager
                val updatedTransaction = Transaction(title, amount, category, date, isIncome)
                val transactions = DataManager.getTransactions(this).toMutableList()
                transactions[transactionIndex] = updatedTransaction
                DataManager.saveTransactions(this, transactions)

                // Finish with a success result
                setResult(RESULT_OK)
                finish()
            }
        } catch (e: Exception) {
            Log.e("EditTransactionActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error starting Edit Transaction: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}