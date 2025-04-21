package com.example.personalfinancetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalfinancetracker.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private var transactions = mutableListOf<Transaction>()
    private var budget: Budget? = null

    // Launcher for exporting data
    private val exportDataLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) {
            Toast.makeText(this, "Export cancelled", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        if (DataManager.exportDataToUri(this, uri)) {
            Toast.makeText(this, "Data exported successfully to selected location", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for importing data (restoring from backup)
    @RequiresApi(Build.VERSION_CODES.M)
    private val importDataLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            Toast.makeText(this, "Restore cancelled", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        if (DataManager.importDataFromUri(this, uri)) {
            Toast.makeText(this, "Data restored successfully", Toast.LENGTH_LONG).show()
            // Refresh the UI with the restored data
            budget = DataManager.getBudget(this)
            transactions = DataManager.getTransactions(this).toMutableList()
            transactionAdapter.updateTransactions(transactions)
            updateUI()
        } else {
            Toast.makeText(this, "Restore failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for editing transactions
    @RequiresApi(Build.VERSION_CODES.M)
    private val editTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Refresh the transaction list from DataManager
            transactions.clear()
            transactions.addAll(DataManager.getTransactions(this))
            transactionAdapter.updateTransactions(transactions)
            updateUI()
            DataManager.clearEditingTransaction() // Clear temporary storage
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        budget = DataManager.getBudget(this)
        transactions = DataManager.getTransactions(this).toMutableList()

        transactionAdapter = TransactionAdapter(
            transactions,
            onDeleteClick = { transaction ->
                DataManager.saveTransactions(this, transactions)
                updateUI()
            },
            onEditClick = { transaction ->
                val index = transactions.indexOf(transaction)
                Log.d("MainActivity", "Editing transaction: $transaction, index: $index")
                DataManager.setEditingTransaction(transaction, index)
                val intent = Intent(this, EditTransactionActivity::class.java)
                editTransactionLauncher.launch(intent)
            }
        )
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = transactionAdapter

        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        binding.btnSetBudget.setOnClickListener {
            startActivity(Intent(this, SetBudgetActivity::class.java))
        }
        binding.btnViewAnalysis.setOnClickListener {
            startActivity(Intent(this, SpendingAnalysisActivity::class.java))
        }
        binding.btnExport.setOnClickListener {
            // Launch the document creation intent with a suggested file name
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportDataLauncher.launch("finance_data_$timestamp.json")
        }
        binding.btnRestore.setOnClickListener {
            // Launch the document picker to select a JSON file for restore
            importDataLauncher.launch(arrayOf("application/json"))
        }

        NotificationHelper.createNotificationChannel(this)
        updateUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        val updatedTransactions = DataManager.getTransactions(this)
        transactionAdapter.updateTransactions(updatedTransactions)
        updateUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        val balance = transactions.sumOf { if (it.isIncome) it.amount else -it.amount }
        binding.tvBalance.text = "Balance: ${budget?.currency ?: "LKR"} %.2f".format(balance)

        val totalExpenses = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val budgetAmount = budget?.monthlyBudget ?: 0.0
        binding.tvBudgetStatus.text = "Budget: ${budget?.currency ?: "LKR"} %.2f / %.2f".format(totalExpenses, budgetAmount)

        if (totalExpenses >= budgetAmount * 0.9) {
            showBudgetWarning(totalExpenses >= budgetAmount)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showBudgetWarning(exceeded: Boolean) {
        NotificationHelper.showBudgetNotification(this, exceeded)
    }
}