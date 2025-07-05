package com.example.exptrackpm.ui.screens.budgets


import androidx.lifecycle.ViewModel
import com.example.exptrackpm.data.users.UserRepository
import com.example.exptrackpm.domain.model.Budget
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BudgetViewModel: ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadBudgets(userId: String) {
        _loading.value = true
        UserRepository.getBudgets(
            //userId
            ) { list ->
            _budgets.value = list
            _loading.value = false
        }
    }

    fun saveBudget(budget: Budget) {
        _loading.value = true
        UserRepository.saveBudget(budget) { success ->
            _loading.value = false
            if (success) {
                loadBudgets(budget.userId)
            }
        }
    }
    fun deleteBudget(budgetId: String) {
        _loading.value = true
        UserRepository.deleteBudget(budgetId) { success ->
            _loading.value = false
            if (success) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (userId.isNotBlank()) {
                    loadBudgets(userId) // Reload budgets after deletion
                }
            }
        }
    }
}
