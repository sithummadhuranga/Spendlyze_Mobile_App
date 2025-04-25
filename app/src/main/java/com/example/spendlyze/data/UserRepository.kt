package com.example.spendlyze.data

import android.content.Context
import android.content.SharedPreferences
import com.example.spendlyze.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val userListType = object : TypeToken<List<User>>() {}.type

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getCurrentUser(): User? {
        val userJson = prefs.getString(KEY_CURRENT_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }

    fun login(email: String, password: String): Boolean {
        val usersJson = prefs.getString(KEY_USERS, "[]")
        val users = gson.fromJson<List<User>>(usersJson, userListType)
        
        val user = users.find { it.email == email }
        if (user == null) {
            return false
        }
        
        // In a real app, we would verify the password here
        // For demo, we'll just check if the password is not empty
        if (password.isEmpty()) {
            return false
        }
        
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER, gson.toJson(user))
            .apply()
            
        return true
    }

    fun signUp(username: String, email: String, password: String): Boolean {
        val usersJson = prefs.getString(KEY_USERS, "[]")
        val users = gson.fromJson<List<User>>(usersJson, userListType)
        
        if (users.any { it.email == email }) {
            return false
        }
        
        // In a real app, we would hash the password here
        // For demo, we'll just check if the password is not empty
        if (password.isEmpty()) {
            return false
        }
        
        val newUser = User(
            id = System.currentTimeMillis().toString(),
            username = username,
            email = email
        )
        
        val updatedUsers = users + newUser
        
        prefs.edit()
            .putString(KEY_USERS, gson.toJson(updatedUsers))
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USER, gson.toJson(newUser))
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
        private const val KEY_USERS = "users"
    }
} 