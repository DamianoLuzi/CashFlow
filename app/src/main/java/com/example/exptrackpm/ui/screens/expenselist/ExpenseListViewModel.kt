package com.example.exptrackpm.ui.screens.expenselist

import androidx.lifecycle.ViewModel
import com.example.exptrackpm.domain.model.Expense
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseListViewModel: ViewModel() {
    private var _expenseList = MutableStateFlow<List<Expense>>(emptyList())
    var expenseList = _expenseList.asStateFlow()

    init {
        getExpenseList()
    }
    fun getExpenseList() {
        var db = Firebase.firestore

        db.collection("expense")
            .addSnapshotListener { value, error ->
                if(error != null) {
                   return@addSnapshotListener
                }
                if (value != null) {
                    _expenseList.value = value.toObjects()
                }
            }
    }
}