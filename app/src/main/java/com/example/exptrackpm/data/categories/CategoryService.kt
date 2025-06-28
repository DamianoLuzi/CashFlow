package com.example.exptrackpm.data.categories

import com.example.exptrackpm.domain.model.Category
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object CategoryService {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    fun addCategory(category: Category, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        db.collection("categories")
            .add(category)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getUserCategories( onData: (List<Category>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                onData(snapshot.toObjects(Category::class.java))
            }
    }
}
