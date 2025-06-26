//package com.example.exptrackpm.ui.screens.transactions
//
//import Transaction
//import TransactionService
//import TransactionType
//import android.app.Application
//import android.util.Log
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.exptrackpm.data.users.UserRepository
//import com.example.exptrackpm.domain.model.Budget
//import com.example.exptrackpm.domain.model.NotificationPreferences
//import com.example.exptrackpm.ui.screens.notifications.NotificationHelper
//import com.google.firebase.Timestamp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//import java.time.Instant
//import java.time.ZoneId
//
//
//class TransactionViewModel (application: Application) : AndroidViewModel(application) {
//    private val auth = Firebase.auth
//    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
//    private val _userNotificationPreferences = MutableStateFlow(NotificationPreferences())
//    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
//    val transactions = _transactions.asStateFlow()
//
//    private val _filter = MutableStateFlow<TransactionType?>(null)
//    val filter = _filter.asStateFlow()
//
//    val filteredTransactions = combine(_transactions, _filter) { all, type ->
//        type?.let { all.filter { it.type == type } } ?: all
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
//
////    init {
////        loadTransactions()
////    }
//
//    init {
//        // CRITICAL FIX: Ensure user ID is available before loading data
//        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
//            loadTransactions() // Pass userId to loadTransactions
//            loadBudgets(userId) // Load budgets for the user
//            loadUserNotificationPreferences(userId) // Load user preferences
//        }
//    }
//
////    fun loadTransactions() {
////        TransactionService.getTransactionsForCurrentUser {
////            _transactions.value = it
////        }
////    }
//
//    private fun loadUserNotificationPreferences(userId: String) {
//        // Assuming UserRepository is designed to fetch user details including preferences
//        UserRepository.getUser { user ->
//            user?.notificationPreferences?.let { preferences ->
//                _userNotificationPreferences.value = preferences
//            }
//        }
//    }
//
//    // Modified loadTransactions to accept userId, assuming TransactionService needs it
//    fun loadTransactions() {
//        TransactionService.getTransactionsForCurrentUser() { // Assuming getTransactionsForCurrentUser now takes userId
//            _transactions.value = it
//        }
//    }
//
//    // Load budgets for the user
//    private fun loadBudgets(userId: String) {
//        UserRepository.getBudgets(userId) { loadedBudgets ->
//            _budgets.value = loadedBudgets
//        }
//    }
//
//    fun setFilter(type: TransactionType?) {
//        _filter.value = type
//    }
//
//    fun addTransaction(
//        amount: Double,
//        description: String,
//        category: String,
//        type: TransactionType,
//        receiptUrl: String? = null
//    ) {
//        val txn = Transaction(
//            userId =  auth.currentUser!!.uid,
//            amount = amount,
//            description = description,
//            category = category,
//            type = type,
//            date = Timestamp.now(),
//            receiptUrl = receiptUrl
//        )
//
//        TransactionService.addTransaction(txn) {
//            loadTransactions()
//        }
//
//        viewModelScope.launch {
//            // Check for over-budget alert after a new expense
//            if (txn.type == TransactionType.EXPENSE && _userNotificationPreferences.value.overBudgetAlerts) {
//                checkForOverBudget(txn.userId, txn.category, txn.amount)
//            }
//        }
//    }
//
//    fun getTransactionById(id: String) = transactions.map { txns ->
//        txns.find { it.id == id }
//    }
//    fun updateTransaction(
//        id: String,
//        amount: Double,
//        description: String,
//        category: String,
//        receiptUrl: String?,
//        date: Timestamp
//    ) {
//        val updatedTxn = transactions.value.find { it.id == id }?.copy(
//            amount = amount,
//            description = description,
//            category = category,
//            receiptUrl = receiptUrl,
//            date = date
//        ) ?: return
//        Log.d("upd", updatedTxn.toString())
//        TransactionService.updateTransaction(updatedTxn) {
//            loadTransactions()
//        }
//    }
//
//    private fun checkForOverBudget(userId: String, category: String, newExpenseAmount: Double) {
//        val budgets = _budgets.value
//        val userPreferences = _userNotificationPreferences.value
//
//        if (!userPreferences.overBudgetAlerts) {
//            return // User doesn't want over budget alerts
//        }
//
//        val budgetForCategory = budgets.find { it.category == category && it.userId == userId }
//        if (budgetForCategory != null) {
//            val currentPeriodExpensesForCategory = _transactions.value
//                .filter {
//                    it.type == TransactionType.EXPENSE &&
//                            it.category == category &&
//                            it.userId == userId &&
//                            it.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1) ==
//                            Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1)
//                }
//                .sumOf { it.amount }
//
//            if (currentPeriodExpensesForCategory > budgetForCategory.amount) {
//                val amountOver = currentPeriodExpensesForCategory - budgetForCategory.amount
//                NotificationHelper.showOverBudgetNotification(
//                    getApplication(),
//                    category,
//                    amountOver
//                )
//            }
//        }
//    }
//
//}
package com.example.exptrackpm.ui.screens.transactions

import Transaction
import TransactionService
import TransactionType
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.Budget
import com.example.exptrackpm.domain.model.NotificationPreferences
import com.example.exptrackpm.ui.screens.notifications.NotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = Firebase.auth
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _userNotificationPreferences = MutableStateFlow(NotificationPreferences())
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val transactions = _transactions.asStateFlow()

    private val _filter = MutableStateFlow<TransactionType?>(null)
    val filter = _filter.asStateFlow()

    val filteredTransactions = combine(_transactions, _filter) { all, type ->
        type?.let { all.filter { it.type == type } } ?: all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            loadTransactions() // Pass userId to loadTransactions
            loadBudgets(userId) // Load budgets for the user
            loadUserNotificationPreferences(userId) // Load user preferences
        } ?: run {
            Log.e("TransactionViewModel", "No authenticated user on ViewModel init. Data will not be loaded.")
        }
    }

    // Load user notification preferences from UserRepository
    private fun loadUserNotificationPreferences(userId: String) {
//        UserRepository.getUser { user ->
//            user?.notificationPreferences?.let { preferences ->
//                _userNotificationPreferences.value = preferences
//            } ?: Log.w("TransactionViewModel", "User or notification preferences not found for $userId.")
//        }
        UserRepository.getUser(userId) { user ->
            user?.notificationPreferences?.let { preferences ->
                _userNotificationPreferences.value = preferences
                Log.d("TransactionViewModel", "Loaded preferences: $preferences")
            } ?: Log.w("TransactionViewModel", "User or notification preferences not found for $userId.")
        }
    }

    fun loadTransactions() {
        TransactionService.getTransactionsForCurrentUser() { transactionsList ->
            _transactions.value = transactionsList
            Log.d("TransactionViewModel", "Transactions loaded: ${transactionsList.size}")
        }
    }

    // Loads budgets for a specific user ID
    private fun loadBudgets(userId: String) {
        UserRepository.getBudgets(userId) { loadedBudgets ->
            _budgets.value = loadedBudgets
            Log.d("TransactionViewModel", "Budgets loaded: ${loadedBudgets.size}")
        }
    }

    // Sets the transaction filter type
    fun setFilter(type: TransactionType?) {
        _filter.value = type
    }

    // Adds a new transaction
    fun addTransaction(
        amount: Double,
        description: String,
        category: String,
        type: TransactionType,
        receiptUrl: String? = null
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            Log.e("TransactionViewModel", "addTransaction: Current user ID is null or blank. Cannot add transaction.")
            return
        }

        val txn = Transaction(
            userId = currentUserId,
            amount = amount,
            description = description,
            category = category,
            type = type,
            date = Timestamp.now(),
            receiptUrl = receiptUrl
        )
        TransactionService.addTransaction(txn) { success ->
            if (success) {
                // Reload all data after successful add to ensure UI is updated and checks run
                currentUserId.let { id ->
                    loadTransactions()
                    loadBudgets(id) // Reload budgets as well, in case check depends on fresh budget data
                    loadUserNotificationPreferences(id)
                }
                viewModelScope.launch {
                    // Check for over-budget alert only if transaction is an expense and preferences allow
                    Log.d("checkOverBudget"," ${txn} ${_userNotificationPreferences.value.overBudgetAlerts}")
                    if (txn.type == TransactionType.EXPENSE && _userNotificationPreferences.value.overBudgetAlerts) {
                        checkForOverBudget(txn.userId, txn.category, txn.amount)
                    }
                }
            } else {
                Log.e("TransactionViewModel", "Failed to add transaction (Service reported failure).")
                // Handle UI feedback for failure
            }
        }
    }

    // Retrieves a transaction by ID
    fun getTransactionById(id: String) = transactions.map { txns ->
        txns.find { it.id == id }
    }

    // Updates an existing transaction
    fun updateTransaction(
        id: String,
        amount: Double,
        description: String,
        category: String,
        receiptUrl: String?,
        date: Timestamp
    ) {
        // Find the existing transaction and create a copy with updated fields
        val updatedTxn = transactions.value.find { it.id == id }?.copy(
            amount = amount,
            description = description,
            category = category,
            receiptUrl = receiptUrl,
            date = date
        ) ?: run {
            Log.e("TransactionViewModel", "updateTransaction: Transaction with ID $id not found for update.")
            return // Exit if original transaction not found
        }
        Log.d("TransactionViewModel", "Attempting to update transaction: $updatedTxn")

        // CRITICAL FIX: Using the onComplete(Boolean) callback from TransactionService
        TransactionService.updateTransaction(updatedTxn) { success ->
            if (success) {
                // Reload data after successful update
                auth.currentUser?.uid?.let { userId ->
                    loadTransactions()
                    loadBudgets(userId) // Reload budgets
                    loadUserNotificationPreferences(userId)
                    viewModelScope.launch {
                        // Re-check for over-budget alerts after an expense update
                        if (updatedTxn.type == TransactionType.EXPENSE && _userNotificationPreferences.value.overBudgetAlerts) {
                            checkForOverBudget(updatedTxn.userId, updatedTxn.category, updatedTxn.amount)
                        }
                    }
                }
            } else {
                Log.e("TransactionViewModel", "Failed to update transaction (Service reported failure).")
                // Handle UI feedback for failure
            }
        }
    }

    // Checks if a budget has been exceeded for a given transaction
    private fun checkForOverBudget(userId: String, category: String, newExpenseAmount: Double) {
        val budgets = _budgets.value
        val userPreferences = _userNotificationPreferences.value

        if (!userPreferences.overBudgetAlerts) {
            Log.d("TransactionViewModel", "Over budget alerts are disabled by user preferences.")
            return
        }

        val budgetForCategory = budgets.find { it.category == category && it.userId == userId }
        if (budgetForCategory != null) {
            // Determine the current month's expenses for this category for the given user
            val currentMonth = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1)
            val currentPeriodExpensesForCategory = _transactions.value
                .filter { txn -> // Renamed 'it' to 'txn' for clarity in nested filters
                    txn.type == TransactionType.EXPENSE &&
                            txn.category == category &&
                            txn.userId == userId &&
                            txn.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1) == currentMonth
                }
                .sumOf { it.amount }

            Log.d("TransactionViewModel", "Checking budget for category '$category'. Budget: €${budgetForCategory.amount}, Current expenses: €$currentPeriodExpensesForCategory")
            if (currentPeriodExpensesForCategory > budgetForCategory.amount) {
                val amountOver = currentPeriodExpensesForCategory - budgetForCategory.amount
                Log.d("TransactionViewModel", "Over budget for '$category' by €$amountOver. Triggering notification.")
                Toast.makeText(getApplication(), "Over budget in $category by €$amountOver", Toast.LENGTH_LONG).show()

                NotificationHelper.showOverBudgetNotification( // Using full path to avoid import conflict if any
                    getApplication(), // Access application context from AndroidViewModel
                    category,
                    amountOver
                )
            } else {
                Log.d("TransactionViewModel", "Still within budget for '$category'.")
            }
        } else {
            Log.d("TransactionViewModel", "No budget found for category '$category' for user $userId.")
        }
    }
}
