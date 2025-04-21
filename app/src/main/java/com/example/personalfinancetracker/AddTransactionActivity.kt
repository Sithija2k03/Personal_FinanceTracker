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

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("AddTransactionActivity", "onCreate called")
            binding = ActivityAddTransactionBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Toolbar
            binding.toolbar?.let {
                setSupportActionBar(it)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)
            } ?: run {
                Log.e("AddTransactionActivity", "Toolbar is null")
            }

            // Spinner
            val categories = arrayOf("Food", "Dining", "Entertainment", "Bills", "Transport", "Personal")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spCategory.adapter = adapter

            // Show DatePickerDialog
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

            // Save button
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

                //Date
                val dateFormatValidator = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormatValidator.isLenient = false
                try {
                    dateFormatValidator.parse(date)
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid date format (use YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val transaction = Transaction(title, amount, category, date, isIncome)
                val transactions = DataManager.getTransactions(this).toMutableList()
                transactions.add(transaction)
                DataManager.saveTransactions(this, transactions)
                Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Log.e("AddTransactionActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error starting Add Transaction: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}