package com.example.adminapp.data.repository

import android.content.Context
import com.example.adminapp.core.MyFirebaseMessagingService
import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.auth.UsersSignUp
import com.example.adminapp.data.model.auth.UserSignIn
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class AuthRepository {

    suspend fun countAdminUsersByEmail(email: String): Int {
        return try {
            val result = supabase.from("users")
                .select(columns = Columns.list("email", "password", "role")) {
                    filter {
                        eq("email", email)
                        eq("role", "admin")
                    }
                }
                .decodeList<UsersSignUp>()
            if (result.isEmpty()) 0 else 1
        } catch (_: Exception) {
            0
        }
    }

    suspend fun signInAdmin(email: String, password: String): UserSignIn? {
        return try {
            val users = supabase.from("users")
                .select(columns = Columns.list("email", "password", "id", "name", "lock")) {
                    filter {
                        eq("email", email)
                        eq("password", password)
                        eq("role", "admin")
                        eq("lock", "active")
                    }
                }
                .decodeList<UserSignIn>()
            users.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signUpAdmin(user: UsersSignUp): Boolean {
        return try {
            val response = supabase.from("users").insert(user)
            println("Insert user response: $response")
            true
        } catch (e: Exception) {
            println("Exception when inserting user: ${'$'}{e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun signOutIfSessionExists() {
        try {
            val currentSession = supabase.auth.currentSessionOrNull()
            if (currentSession != null) {
                supabase.auth.signOut()
            }
        } catch (_: Exception) {
        }
    }

    fun uploadFcmToken(context: Context, userId: String) {
        MyFirebaseMessagingService.generateAndUploadToken(context, userId)
        MyFirebaseMessagingService.uploadPendingToken(context, userId)
    }
}