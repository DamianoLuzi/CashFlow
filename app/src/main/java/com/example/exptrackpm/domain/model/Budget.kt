package com.example.exptrackpm.domain.model

data class Budget(
    val id: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val currency: String = "EUR",
    val userId: String = "",
)