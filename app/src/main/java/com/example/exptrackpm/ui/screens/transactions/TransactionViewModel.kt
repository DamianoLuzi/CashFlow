package com.example.exptrackpm.ui.screens.transactions

import Transaction
import TransactionService
import TransactionType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


class TransactionViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _filter = MutableStateFlow<TransactionType?>(null)
    val filter = _filter.asStateFlow()

    val filteredTransactions = combine(_transactions, _filter) { all, type ->
        type?.let { all.filter { it.type == type } } ?: all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        TransactionService.getTransactionsForCurrentUser {
            _transactions.value = it
        }
    }

    fun setFilter(type: TransactionType?) {
        _filter.value = type
    }

    fun addTransaction(
        amount: Double,
        description: String,
        category: String,
        type: TransactionType,
        receiptUrl: String? = null
    ) {
        val txn = Transaction(
            userId =  auth.currentUser!!.uid,
            amount = amount,
            description = description,
            category = category,
            type = type,
            date = Timestamp.now(),
            receiptUrl = receiptUrl
        )

        TransactionService.addTransaction(txn) {
            loadTransactions()
        }
    }

    fun getUserCategories(any: Any) {

    }

    fun getTransactionById(id: String) = transactions
        .combine(transactions) { txns, _ ->
            txns.find { it.id == id }
        }

    fun updateTransaction(
        id: String,
        amount: Double,
        description: String,
        category: String,
        receiptUrl: String?
    ) {
        val updatedTxn = transactions.value.find { it.id == id }?.copy(
            amount = amount,
            description = description,
            category = category,
            receiptUrl = receiptUrl
        ) ?: return

        TransactionService.updateTransaction(updatedTxn) {
            loadTransactions()
        }
    }

}
