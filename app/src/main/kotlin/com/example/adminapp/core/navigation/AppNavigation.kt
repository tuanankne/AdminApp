package com.example.adminapp.core.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adminapp.core.network.MapboxGeocodingService
import com.example.adminapp.ui.auth.AuthViewModel
import com.example.adminapp.core.supabase
import com.example.adminapp.ui.auth.LoginScreen
import com.example.adminapp.ui.auth.RegisterScreen
import com.example.adminapp.ui.dashboard.AdminDashboardScreen
import com.example.adminapp.ui.provider.ProviderDetailScreen
import com.example.adminapp.ui.provider.ProviderDetailViewModel
import com.example.adminapp.ui.user.UserManagementScreen
import com.example.adminapp.ui.user.UserManagementViewModel
import com.example.adminapp.data.model.Provider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun AppNavigation(initialRoute: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                // Nếu đã đăng nhập, có thể thêm logic chuyển hướng ở đây
                // Hiện tại chỉ hiển thị màn hình đăng nhập
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                onLoginSuccess = {
                    // Chuyển đến trang quản lý admin sau khi đăng nhập thành công
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGotoRegister = {
                    navController.navigate("register")
                },
                viewModel = authViewModel
            )
        }
        composable("register") {
            val authViewModel : AuthViewModel = viewModel()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.mapbox.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val geocodingService = retrofit.create(MapboxGeocodingService::class.java)
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                goBack = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                viewModel = authViewModel,
            )
        }
        composable("dashboard") {
            val authViewModel: AuthViewModel = viewModel()
            val context = LocalContext.current
            AdminDashboardScreen(
                onLogout = {
                    authViewModel.logout(
                        context = context,
                        onLogoutComplete = {
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    )
                },
                onUserManagementClick = {
                    navController.navigate("user_management")
                }
            )
        }
        composable("user_management") {
            val userManagementViewModel: UserManagementViewModel = viewModel()
            
            LaunchedEffect(Unit) {
                userManagementViewModel.loadUsers()
            }
            
            UserManagementScreen(
                providers = userManagementViewModel.providers,
                customers = userManagementViewModel.customers,
                isLoading = userManagementViewModel.isLoading,
                error = userManagementViewModel.error,
                onBack = {
                    navController.popBackStack()
                },
                onProviderClick = { provider ->
                    navController.navigate("provider_detail/${provider.id}")
                },
                onCustomerClick = { customer ->
                    // TODO: Implement customer detail screen if needed
                },
                onDeleteProvider = { providerId ->
                    userManagementViewModel.deleteProvider(providerId)
                },
                onDeleteCustomer = { customerId ->
                    userManagementViewModel.deleteCustomer(customerId)
                }
            )
        }
        composable("provider_detail/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
            val providerDetailViewModel: ProviderDetailViewModel = viewModel()
            
            LaunchedEffect(providerId) {
                providerDetailViewModel.loadProviderDetail(providerId)
            }
            
            ProviderDetailScreen(
                provider = providerDetailViewModel.provider,
                services = providerDetailViewModel.services,
                isLoading = providerDetailViewModel.isLoading,
                error = providerDetailViewModel.error,
                onBack = {
                    navController.popBackStack()
                },
                isUpdatingLock = providerDetailViewModel.isUpdatingLock,
                onToggleLock = {
                    providerDetailViewModel.toggleLockStatus()
                }
            )
        }
    }
}