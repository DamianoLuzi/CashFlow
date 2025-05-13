package com.example.exptrackpm.ui.screens.categories

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoryViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    init {

    }

    fun addCategory(name: String, icon: String?, color: String?) {
        val userId = auth.currentUser?.uid ?: return
        val categoryData = hashMapOf(
            "name" to name,
            "icon" to icon,
            "color" to color,
            "createdAt" to Timestamp.now()
        )

        db.collection("users")
            .document(userId)
            .collection("categories")
            .add(categoryData)
    }

    fun getUserCategories(onResult: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                val categories = snapshot.documents.mapNotNull { it.getString("name") }
                onResult(categories)
            }
    }
}
