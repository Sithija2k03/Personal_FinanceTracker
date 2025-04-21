package com.example.personalfinancetracker

data class Budget(
    val monthlyBudget: Double,
    val currency: String = "LKR"
)