package com.example.exptrackpm.domain.model

import com.google.firebase.Timestamp

data class Category(
    val userId: String = "",
    val name: String = "",
    val icon: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)
