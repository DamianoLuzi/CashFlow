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
            loadTransactions()
            loadBudgets(userId)
            loadUserNotificationPreferences(userId)
        } ?: run {
            Log.e("TransactionViewModel", "No authenticated user on ViewModel init. Data will not be loaded.")
        }
    }
    private fun loadUserNotificationPreferences(userId: String) {
        UserRepository.getUser(//userId
             ) { user ->
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
    private fun loadBudgets(userId: String) {
        UserRepository.getBudgets(
            //userId
            ) { loadedBudgets ->
            _budgets.value = loadedBudgets
            Log.d("TransactionViewModel", "Budgets loaded: ${loadedBudgets.size}")
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

    fun getTransactionById(id: String) = transactions.map { txns ->
        txns.find { it.id == id }
    }

    fun updateTransaction(
        id: String,
        amount: Double,
        description: String,
        category: String,
        receiptUrl: String?,
        date: Timestamp
    ) {
        val updatedTxn = transactions.value.find { it.id == id }?.copy(
            amount = amount,
            description = description,
            category = category,
            receiptUrl = receiptUrl,
            date = date
        ) ?: run {
            Log.e("TransactionViewModel", "updateTransaction: Transaction with ID $id not found for update.")
            return
        }
        Log.d("TransactionViewModel", "Attempting to update transaction: $updatedTxn")
        TransactionService.updateTransaction(updatedTxn) { success ->
            if (success) {
                auth.currentUser?.uid?.let { userId ->
                    loadTransactions()
                    loadBudgets(userId) // Reload budgets
                    loadUserNotificationPreferences(userId)
                    viewModelScope.launch {
                        if (updatedTxn.type == TransactionType.EXPENSE && _userNotificationPreferences.value.overBudgetAlerts) {
                            checkForOverBudget(updatedTxn.userId, updatedTxn.category, updatedTxn.amount)
                        }
                    }
                }
            } else {
                Log.e("TransactionViewModel", "Failed to update transaction (Service reported failure).")
            }
        }
    }

    private fun checkForOverBudget(userId: String, category: String, newExpenseAmount: Double) {
        val budgets = _budgets.value
        val userPreferences = _userNotificationPreferences.value

        if (!userPreferences.overBudgetAlerts) {
            Log.d("TransactionViewModel", "Over budget alerts are disabled by user preferences.")
            return
        }

        val budgetForCategory = budgets.find { it.category == category && it.userId == userId }
        if (budgetForCategory != null) {
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
