package com.example.exptrackpm.ui.screens.addexpense

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.exptrackpm.domain.model.Expense
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AddExpenseViewModel : ViewModel(){

    private val db = Firebase.firestore

    fun addExpense(amount: Double, description: String, category: String, receiptUrl: String?) {

        val expense = Expense(
            id = "",
            userId = "user123",
            amount = amount,
            description = description,
            category = category,
            date = Timestamp.now(),
            receiptUrl = receiptUrl
        )


        db.collection("expense")
            .add(expense)
            .addOnSuccessListener {
                Log.d("document", "Expense Successfully Created!")
            }
    }
}