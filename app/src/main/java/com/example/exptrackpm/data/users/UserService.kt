package com.example.exptrackpm.data.users

import com.example.exptrackpm.domain.model.Budget
import com.example.exptrackpm.domain.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

object UserRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun getUser(onResult: (User?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                onResult(user)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun updateUser(user: User, onComplete: (Boolean) -> Unit) {
        firestore.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getBudgets(userId: String, onResult: (List<Budget>) -> Unit) {
        firestore.collection("users").document(userId)
            .collection("budgets").get()
            .addOnSuccessListener { query ->
                val budgets = query.documents.mapNotNull { it.toObject(Budget::class.java) }
                onResult(budgets)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun saveBudget(budget: Budget, onComplete: (Boolean) -> Unit) {
        val budgetRef = if (budget.id.isBlank()) {
            firestore.collection("users").document(budget.userId)
                .collection("budgets").document()
        } else {
            firestore.collection("users").document(budget.userId)
                .collection("budgets").document(budget.id)
        }

        budgetRef.set(budget)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}