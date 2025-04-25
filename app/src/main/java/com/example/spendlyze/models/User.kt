package com.example.spendlyze.models

data class User(
    val id: String,
    val username: String,
    val email: String,
    val profileImageUrl: String = "https://ui-avatars.com/api/?name=$username&background=random"
) 