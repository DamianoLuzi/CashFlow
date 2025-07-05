package com.example.exptrackpm.data.categories

import android.util.Log
import com.example.exptrackpm.domain.model.Category
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object CategoryService {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    fun addCategory(category: Category, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
//        db.collection("categories")
//            .add(category)
//            .addOnSuccessListener { onSuccess() }
//            .addOnFailureListener { onFailure(it) }
        val collectionRef = db.collection("categories")
        val documentRef = if (category.id.isNullOrBlank()) { // Check if ID is already set (for updates)
            collectionRef.document() // Generate a new ID if not set
        } else {
            collectionRef.document(category.id) // Use existing ID for updates
        }

        // Create a new Category object with the assigned ID (either new or existing)
        val categoryWithId = category.copy(id = documentRef.id)

        documentRef.set(categoryWithId) // Use .set() to either add or update based on ID
            .addOnSuccessListener {
                Log.d("CategoryService", "Category with ID ${categoryWithId.id} added/updated successfully!")
                onSuccess()
            }
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
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CategoryService", "Error listening for categories: $error")
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    onData(emptyList())
                    return@addSnapshotListener
                }

                val categories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                onData(categories)
            }
    }


    fun deleteCategory(categoryId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        db.collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener {
                Log.d("CategoryService", "Category with ID $categoryId successfully deleted!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("CategoryService", "Error deleting category with ID $categoryId: ${e.message}", e)
                onFailure(e)
            }
    }
}
