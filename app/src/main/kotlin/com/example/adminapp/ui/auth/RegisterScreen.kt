package com.example.adminapp.ui.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminapp.BuildConfig
import com.example.adminapp.core.network.MapboxPlace
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import com.example.adminapp.core.network.MapboxGeocodingService
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    goBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isValid = phoneNumber.matches(Regex("^0[0-9]{9}$"))
    val isValidEmail = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))
    val isLoading = viewModel.isLoading
    val authError = viewModel.authError

    // Animation states
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0.7f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFFf093fb)
                    )
                )
            )
    ) {
        // Background decoration
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f)
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = (-50).dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(100.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 300.dp, y = 100.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(75.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 50.dp, y = 500.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(50.dp)
                    )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .alpha(alpha),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Header Section
            item {
                RegisterHeader()
            }

            // Personal Info Section
            item {
                PersonalInfoSection(
                    name = name,
                    onNameChange = { name = it },
                    email = email,
                    onEmailChange = { email = it },
                    isValidEmail = isValidEmail,
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
            }

            // Contact Info Section
            item {
                ContactInfoSection(
                    phoneNumber = phoneNumber,
                    onPhoneChange = { phoneNumber = it },
                    isValidPhone = isValid,
                )
            }

            // Error Message
            authError?.let { error ->
                item {
                    ErrorMessage(error)
                }
            }

            // Action Buttons
            item {
                ActionButtonsSection(
                    isLoading = isLoading,
                    isFormValid = name.isNotEmpty() && isValidEmail && password.isNotEmpty() && isValid,
                    onRegister = {
                        if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && phoneNumber.isNotEmpty()) {
                            viewModel.clearError()
                            coroutineScope.launch {
                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    address = "Company Admin", // Địa chỉ mặc định
                                    name = name,
                                    phoneNumber = phoneNumber,
                                    onSuccess = {
                                        onRegisterSuccess()
                                    }
                                )
                            }
                        } else {
                            viewModel.authError = "Vui lòng điền đầy đủ thông tin"
                        }
                    },
                    onGoBack = goBack
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun RegisterHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Icon/Logo with modern design
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.7f)
                        )
                    ),
                    RoundedCornerShape(25.dp)
                )
                .clip(RoundedCornerShape(25.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Register",
                modifier = Modifier.size(50.dp),
                tint = Color(0xFF667eea)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tạo tài khoản mới",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Điền thông tin để tạo tài khoản của bạn",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun PersonalInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    isValidEmail: Boolean,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit
) {
    FormCard(
        title = "Thông tin cá nhân",
        icon = Icons.Default.Person,
        description = "Nhập thông tin cơ bản của bạn"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Tên người dùng",
                icon = Icons.Default.Person,
                placeholder = "Nhập tên của bạn"
            )

            CustomTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email",
                icon = Icons.Default.Email,
                placeholder = "example@email.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.isNotEmpty() && !isValidEmail,
                supportingText = if (email.isNotEmpty() && !isValidEmail) "Email không hợp lệ" else null
            )

            CustomTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = "Mật khẩu",
                icon = Icons.Default.Lock,
                placeholder = "Nhập mật khẩu",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityChange) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ContactInfoSection(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    isValidPhone: Boolean,
) {
    FormCard(
        title = "Thông tin liên hệ",
        icon = Icons.Default.Phone,
        description = "Nhập số điện thoại của bạn"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                label = "Số điện thoại",
                icon = Icons.Default.Phone,
                placeholder = "0123456789",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumber.isNotEmpty() && !isValidPhone,
                supportingText = if (phoneNumber.isNotEmpty() && !isValidPhone) "Số điện thoại không hợp lệ" else null
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isLoading: Boolean,
    isFormValid: Boolean,
    onRegister: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !isLoading && isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667eea)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Đang đăng ký...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Register",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Đăng ký",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        OutlinedButton(
            onClick = onGoBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF667eea)
            ),
            border = BorderStroke(2.dp, Color(0xFF667eea)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quay lại",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        if (!isFormValid) {
            Text(
                text = "Vui lòng điền đầy đủ thông tin hợp lệ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4A5568),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun FormCard(
    title: String,
    icon: ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFF667eea).copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFF667eea),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748),
                        fontSize = 18.sp
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A5568),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = label,
                    color = if (isError) Color(0xFFE53E3E) else Color(0xFF4A5568),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ) 
            },
            placeholder = { 
                Text(
                    text = placeholder,
                    color = Color(0xFFA0AEC0),
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isError) Color(0xFFE53E3E) else Color(0xFF667eea),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Color(0xFFE53E3E) else Color(0xFF667eea),
                unfocusedBorderColor = if (isError) Color(0xFFE53E3E) else Color(0xFFE2E8F0),
                errorBorderColor = Color(0xFFE53E3E),
                focusedLabelColor = if (isError) Color(0xFFE53E3E) else Color(0xFF667eea),
                unfocusedLabelColor = if (isError) Color(0xFFE53E3E) else Color(0xFF4A5568),
                focusedTextColor = Color(0xFF2D3748),
                unfocusedTextColor = Color(0xFF2D3748),
                focusedContainerColor = Color(0xFFF7FAFC),
                unfocusedContainerColor = Color(0xFFF7FAFC)
            )
        )

        supportingText?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53E3E),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFED7D7)
        ),
        border = BorderStroke(1.dp, Color(0xFFFC8181)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color(0xFFE53E3E),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC53030),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
