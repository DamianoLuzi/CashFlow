package com.example.exptrackpm.data.users

import android.util.Log
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
                val user = doc.toObject(User::class.java)?.copy(id = doc.id)
                onResult(user)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun updateUser(user: User, onComplete: (Boolean) -> Unit) {

        if (user.id.isBlank()) {
            Log.e("UserRepository", "updateUser: Cannot update user. User ID is blank. User data: $user")
            onComplete(false)
            return
        }

        val currentAuthUid = auth.currentUser?.uid
        if (currentAuthUid.isNullOrBlank() || currentAuthUid != user.id) {
            // This is a safety check. If the user object's ID doesn't match the currently authenticated UID,
            // it indicates a potential logic error or security concern.
            Log.e("UserRepository", "updateUser: Mismatch between authenticated UID ($currentAuthUid) and user object ID (${user.id}). Aborting update.")
            onComplete(false)
            return
        }
        firestore.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getBudgets(userId: String, onResult: (List<Budget>) -> Unit) {
        firestore.collection("budgets")
            .whereEqualTo("userId", userId) // Filter budgets by the user ID
            .get()
            .addOnSuccessListener { query ->
                val budgets = query.documents.mapNotNull { doc ->
                    // Still ensure 'id' is set from document ID, 'userId' should already be in the Budget object
                    doc.toObject(Budget::class.java)?.copy(id = doc.id)
                }
                Log.d("UserRepository", "Fetched ${budgets.size} budgets for user $userId.")
                onResult(budgets)
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "getBudgets: Error fetching budgets for user $userId", e)
                onResult(emptyList())
            }
    }

    fun saveBudget(budget: Budget, onComplete: (Boolean) -> Unit) {
        val budgetRef = if (budget.id.isBlank()) {
            firestore.collection("budgets").document()
        } else {
            firestore.collection("budgets").document(budget.id)
        }
        val finalBudget = if (budget.id.isBlank()) {
            budget.copy(id = budgetRef.id)
        } else {
            budget
        }

        budgetRef.set(finalBudget)
            .addOnSuccessListener {
                Log.d("UserRepository", "Budget with ID ${finalBudget.id} saved successfully for user ${finalBudget.userId}.")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to save budget with ID ${finalBudget.id} for user ${finalBudget.userId}", e)
                onComplete(false)
            }
}}