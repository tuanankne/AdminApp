package com.example.adminapp.ui.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.adminapp.data.model.Provider
import com.example.adminapp.data.model.ProviderServiceDetail
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    provider: Provider?,
    services: List<ProviderServiceDetail>,
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    isUpdatingLock: Boolean = false,
    onToggleLock: () -> Unit = {},
    onViewOrders: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Chi tiết nhà cung cấp",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    if (provider != null) {
                        IconButton(
                            onClick = { 
                                onViewOrders(provider.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = "Xem đơn hàng",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (provider != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = onToggleLock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        enabled = !isUpdatingLock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (provider.lock == "locked") Color(0xFF48BB78) else Color(0xFFE53E3E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUpdatingLock) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = if (provider.lock == "locked") Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (provider.lock == "locked") "Mở khóa tài khoản" else "Khóa tài khoản",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Lỗi",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (provider != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Provider Info Card
                item {
                    ProviderInfoCard(provider = provider)
                }
                
                // Services List
                item {
                    Text(
                        text = "Danh sách dịch vụ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (services.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Không có dịch vụ",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nhà cung cấp chưa có dịch vụ nào",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(services) { service ->
                        ServiceItemCard(service = service)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderInfoCard(provider: Provider) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (provider.avatar != null && provider.avatar.isNotEmpty()) {
                    AsyncImage(
                        model = provider.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Provider Name
            Text(
                text = provider.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3748)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Provider ID
            Text(
                text = "ID: ${provider.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF718096)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Info Rows
            InfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = provider.email
            )
            
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Địa chỉ",
                value = provider.address ?: "Chưa cập nhật"
            )
            
            InfoRow(
                icon = Icons.Default.Phone,
                label = "Số điện thoại",
                value = provider.phoneNumber ?: "Chưa cập nhật"
            )
            
            InfoRow(
                icon = Icons.Default.Lock,
                label = "Mật khẩu",
                value = "••••••••"
            )
            
            // PayPal Email (if available)
            provider.paypal_email?.let { paypalEmail ->
                InfoRow(
                    icon = Icons.Default.Payment,
                    label = "PayPal Email",
                    value = paypalEmail
                )
            }
            
            // Rating (if available)
            provider.averageRating?.let { rating ->
                InfoRow(
                    icon = Icons.Default.Star,
                    label = "Điểm đánh giá",
                    value = String.format("%.1f/5.0", rating)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF667eea),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF718096),
                fontSize = 12.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2D3748),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ServiceItemCard(service: ProviderServiceDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = service.services.serviceTypes.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748)
                )
                Text(
                    text = service.services.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A5568)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Giá dịch vụ: ${String.format(Locale.US, "%.0f", service.customPrice)} VNĐ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667eea),
                textAlign = TextAlign.End
            )
        }
    }
}
