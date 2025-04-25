 package com.example.spendlyze.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.spendlyze.models.User
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getCurrentUser(): User? {
        val userJson = prefs.getString(KEY_CURRENT_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }

    fun login(email: String, password: String): Boolean {
        // In a real app, this would make an API call
        // For demo, we'll just create a mock user
        val user = User(
            id = "1",
            username = email.split("@")[0],
            email = email
        )
        
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER, gson.toJson(user))
            .apply()
            
        return true
    }

    fun signUp(username: String, email: String, password: String): Boolean {
        // In a real app, this would make an API call
        // For demo, we'll just create a mock user
        val user = User(
            id = "1",
            username = username,
            email = email
        )
        
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER, gson.toJson(user))
            .apply()
            
        return true
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_CURRENT_USER)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER = "current_user"
    }
}