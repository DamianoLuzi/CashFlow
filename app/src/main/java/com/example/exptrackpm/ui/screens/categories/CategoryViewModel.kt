package com.example.exptrackpm.ui.screens.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.exptrackpm.data.categories.CategoryService
import com.example.exptrackpm.domain.model.Category
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryViewModel : ViewModel() {
    private val auth = Firebase.auth
//    var categories by mutableStateOf<List<Category>>(emptyList())
//        private set

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    var name by mutableStateOf("")
    var icon by mutableStateOf<String?>(null)
    var color by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)

    init {
        loadCategories()
    }

    fun loadCategories() {
        CategoryService.getUserCategories {
            _categories.value = it
        }
    }

    fun addCategory(
        name: String,
        icon: String,
        color: String
    ) {
        val uid = auth.currentUser?.uid ?: return

        if (name.isBlank()) {
            error = "Category name can't be blank!"
            return
        }

        var newCategory = Category(
            userId = uid,
            name = name,
            icon = icon,
            color = color
        )

        CategoryService.addCategory(newCategory, {
            loadCategories()
        }, {
            error = it.localizedMessage
        })
    }
}

