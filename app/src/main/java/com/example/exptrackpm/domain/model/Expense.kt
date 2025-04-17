package com.example.exptrackpm.domain.model

import com.google.firebase.Timestamp


//data class Expense(
//    //val id: String = "",
//   // val userId: String = "",
//    val amount: Double = 0.0,
//    //val description: String = "",
//    val category: String = "",
//    val date: Timestamp = Timestamp.now()
//    //val date: Long = 0L,
//    //val receiptUrl: String? = null
//)


data class Expense(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val date: Timestamp = Timestamp.now(),
    val receiptUrl: String? = null
)
