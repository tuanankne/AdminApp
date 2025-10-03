package com.example.adminapp.data.repository

import android.content.Context
import com.example.adminapp.core.MyFirebaseMessagingService
import com.example.adminapp.core.supabase
import com.example.adminapp.data.model.auth.AuthDtos
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.model.ProviderServiceDetail
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class AuthRepository {

    suspend fun countAdminUsersByEmail(email: String): Int {
        return try {
            val result = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("role", "admin")
                    }
                }
                .decodeList<AuthDtos.UsersSignUp>()
            if (result.isEmpty()) 0 else 1
        } catch (_: Exception) {
            0
        }
    }

    suspend fun signInAdmin(email: String, password: String): AuthDtos.UserSignIn? {
        return try {
            val users = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("password", password)
                        eq("role", "admin")
                    }
                }
                .decodeList<AuthDtos.UserSignIn>()
            users.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signUpAdmin(user: AuthDtos.UsersSignUp): Boolean {
        return try {
            supabase.from("users").insert(user)
            true
        } catch (_: Exception) {
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

    suspend fun fetchProviderById(providerId: String): Provider? {
        return try {
            val providers = supabase.from("users")
                .select {
                    filter {
                        eq("id", providerId)
                        eq("role", "provider")
                    }
                }
                .decodeList<Provider>()
            providers.firstOrNull()
        } catch (e: Exception) {
            println("Error fetching provider: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchProviderServicesDetail(providerId: String): List<ProviderServiceDetail> {
        return try {
            supabase.from("provider_services")
                .select {
                    filter {
                        eq("provider_id", providerId)
                    }
                }
                .decodeList<ProviderServiceDetail>()
        } catch (e: Exception) {
            println("Error fetching provider services: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProviders(): List<Provider> {
        return try {
            supabase.from("users")
                .select {
                    filter {
                        eq("role", "provider")
                    }
                }
                .decodeList<Provider>()
        } catch (e: Exception) {
            println("Error fetching providers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCustomers(): List<Provider> {
        return try {
            supabase.from("users")
                .select {
                    filter {
                        eq("role", "customer")
                    }
                }
                .decodeList<Provider>()
        } catch (e: Exception) {
            println("Error fetching customers: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            supabase.from("users")
                .delete {
                    filter {
                        eq("id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            println("Error deleting user: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}