package com.example.adminapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onUserManagementClick: () -> Unit,
    onOrderManagementClick: () -> Unit,
    onStatisticsClick: () -> Unit = {},
    onServiceManagementClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Trang Quản Lý Admin",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Đăng xuất"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeCard()
            }
            item {
                Text(
                    text = "Chức năng quản lý",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(getAdminFeatures(onUserManagementClick, onOrderManagementClick, onStatisticsClick, onServiceManagementClick)) { feature ->
                AdminFeatureCard(
                    title = feature.title,
                    description = feature.description,
                    icon = feature.icon,
                    onClick = feature.onClick
                )
            }
        }
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Chào mừng Admin!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quản lý hệ thống và người dùng một cách hiệu quả",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AdminFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Mở",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class AdminFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private fun getAdminFeatures(
    onUserManagementClick: () -> Unit,
    onOrderManagementClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onServiceManagementClick: () -> Unit
): List<AdminFeature> {
    return listOf(
        AdminFeature(
            title = "Quản lý người dùng",
            description = "Xem, thêm, sửa, xóa thông tin người dùng",
            icon = Icons.Default.People,
            onClick = onUserManagementClick
        ),
        AdminFeature(
            title = "Quản lý đơn hàng",
            description = "Theo dõi và xử lý các đơn hàng",
            icon = Icons.Default.ShoppingCart,
            onClick = onOrderManagementClick
        ),
        AdminFeature(
            title = "Thống kê báo cáo",
            description = "Xem các báo cáo và thống kê hệ thống",
            icon = Icons.Default.Analytics,
            onClick = onStatisticsClick
        ),
        AdminFeature(
            title = "Quản lý dịch vụ",
            description = "Quản lý các dịch vụ cung cấp",
            icon = Icons.Default.Build,
            onClick = onServiceManagementClick
        ),
        AdminFeature(
            title = "Khuyến mại",
            description = "Quản lý các chương trình khuyến mại",
            icon = Icons.Default.LocalOffer,
            onClick = {}
        ),
        AdminFeature(
            title = "Hỗ trợ",
            description = "Xem và xử lý các yêu cầu hỗ trợ",
            icon = Icons.Default.Support,
            onClick = {}
        )
    )
}
