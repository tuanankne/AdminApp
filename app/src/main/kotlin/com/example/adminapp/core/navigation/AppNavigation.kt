package com.example.adminapp.core.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adminapp.ui.auth.AuthViewModel
import com.example.adminapp.ui.auth.LoginScreen
import com.example.adminapp.ui.auth.RegisterScreen
import com.example.adminapp.ui.dashboard.AdminDashboardScreen
import com.example.adminapp.ui.user.UserManagementScreen
import com.example.adminapp.ui.user.UserManagementViewModel
import com.example.adminapp.ui.provider.ProviderDetailScreen
import com.example.adminapp.ui.provider.ProviderDetailViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGotoRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                goBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
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
            val userViewModel: UserManagementViewModel = viewModel()
            
            // Load users when screen appears
            LaunchedEffect(Unit) {
                userViewModel.loadUsers()
            }

            UserManagementScreen(
                providers = userViewModel.providers,
                customers = userViewModel.customers,
                isLoading = userViewModel.isLoading,
                error = userViewModel.error,
                onBack = { navController.popBackStack() },
                onProviderClick = { provider ->
                    navController.navigate("provider_detail/${provider.id}")
                },
                onCustomerClick = { customer ->
                    // TODO: Navigate to customer detail
                },
                onDeleteProvider = { providerId ->
                    userViewModel.deleteProvider(providerId)
                },
                onDeleteCustomer = { customerId ->
                    userViewModel.deleteCustomer(customerId)
                }
            )
        }

        composable("provider_detail/{providerId}") { backStackEntry ->
            val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
            val viewModel: ProviderDetailViewModel = viewModel()

            // Load provider detail when screen appears
            LaunchedEffect(providerId) {
                viewModel.loadProviderDetail(providerId)
            }

            ProviderDetailScreen(
                provider = viewModel.provider,
                services = viewModel.services,
                isLoading = viewModel.isLoading,
                error = viewModel.error,
                onBack = { navController.popBackStack() }
            )
        }
    }
}