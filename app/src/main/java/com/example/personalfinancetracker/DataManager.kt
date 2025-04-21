package com.example.personalfinancetracker

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object DataManager {
    private const val PREFS_NAME = "FinancePrefs"
    private const val KEY_BUDGET = "budget"
    private const val KEY_TRANSACTIONS = "transactions"

    private var editingTransaction: Transaction? = null
    private var editingTransactionIndex: Int = -1

    fun setEditingTransaction(transaction: Transaction?, index: Int) {
        editingTransaction = transaction
        editingTransactionIndex = index
    }

    fun getEditingTransaction(): Transaction? = editingTransaction

    fun getEditingTransactionIndex(): Int = editingTransactionIndex

    fun clearEditingTransaction() {
        editingTransaction = null
        editingTransactionIndex = -1
    }

    fun saveBudget(context: Context, budget: Budget) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(budget)
        editor.putString(KEY_BUDGET, json)
        editor.apply()
    }

    fun getBudget(context: Context): Budget? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(KEY_BUDGET, null)
        return if (json != null) {
            gson.fromJson(json, Budget::class.java)
        } else {
            null
        }
    }

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(transactions)
        editor.putString(KEY_TRANSACTIONS, json)
        editor.apply()
    }

    fun getTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun exportDataToUri(context: Context, uri: Uri): Boolean {
        return try {
            val budget = getBudget(context)
            val transactions = getTransactions(context)
            val data = mapOf("budget" to budget, "transactions" to transactions)
            val gson = Gson()
            val json = gson.toJson(data)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDataFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val gson = Gson()
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(json, type)
            val budgetJson = gson.toJson(data["budget"])
            val transactionsJson = gson.toJson(data["transactions"])
            val budget = if (budgetJson != null && budgetJson != "null") {
                gson.fromJson(budgetJson, Budget::class.java)
            } else {
                null
            }
            val transactions = if (transactionsJson != null && transactionsJson != "null") {
                val transactionsType = object : TypeToken<List<Transaction>>() {}.type
                gson.fromJson(transactionsJson, transactionsType)
            } else {
                emptyList<Transaction>()
            }
            saveBudget(context, budget ?: Budget(0.0, "LKR"))
            saveTransactions(context, transactions)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}