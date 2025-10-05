package com.example.adminapp.ui.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.Instant
import java.time.ZoneId


// Helper function to format date strings from Supabase
fun formatDateString(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Không có thông tin"
    
    return try {
        // Parse ISO datetime from Supabase
        val instant = Instant.parse(dateString)
        // Convert to Vietnam timezone (UTC+7)
        val vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh")
        val vietnamTime = instant.atZone(vietnamZone)
        // Format theo kiểu Việt Nam
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        vietnamTime.format(formatter)
    } catch (e: Exception) {
        // Nếu parse lỗi thì thử cách khác
        try {
            val dateTime = LocalDateTime.parse(dateString.replace("Z", "").substring(0, 19))
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            dateTime.format(formatter)
        } catch (e2: Exception) {
            dateString // Trả về string gốc nếu không parse được
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    orderId: Long,
    orderViewModel: OrderManagementViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val orders = orderViewModel.orders
    val isLoading = orderViewModel.isLoading
    val error = orderViewModel.error

    LaunchedEffect(orderId) {
        orderViewModel.loadOrderDetail(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Chi tiết đơn hàng #$orderId")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { orderViewModel.loadOrderDetail(orderId) }) {
                            Text("Thử lại")
                        }
                    }
                }
                orders.isNotEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            items(orders) { orderDetail ->
                                OrderCard(
                                    orderDetail = orderDetail,
                                    orderViewModel = orderViewModel
                                )
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = "Không có dữ liệu đơn hàng",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    orderDetail: OrderDetail,
    orderViewModel: OrderManagementViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Modern Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Đơn hàng #${orderDetail.booking.id}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tạo lúc: ${formatDateString(orderDetail.booking.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    ModernStatusChip(status = orderDetail.booking.status)
                }
            }
            
            // Content
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // Service Information Section
                ModernInfoSection(
                    title = "Thông tin dịch vụ",
                    icon = Icons.Default.Build
                ) {
                    ModernInfoRow(
                        label = "Tên dịch vụ",
                        value = orderDetail.providerService?.services?.name ?: "Chưa tải được thông tin dịch vụ",
                        icon = Icons.Default.Build,
                        isHighlight = true
                    )
                    ModernInfoRow(
                        label = "Số lượng nhân viên",
                        value = "${orderDetail.booking.numberWorkers ?: 0} người",
                        icon = Icons.Default.Group
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Customer Information Section
                ModernInfoSection(
                    title = "Thông tin khách hàng",
                    icon = Icons.Default.Person
                ) {
                    ModernInfoRow(
                        label = "Người thuê",
                        value = orderDetail.customer?.name ?: orderDetail.customer?.email ?: "Không có thông tin",
                        icon = Icons.Default.Person,
                        isHighlight = true
                    )
                    orderDetail.customer?.phone_number?.let { phone ->
                        ModernInfoRow(
                            label = "Số điện thoại",
                            value = phone,
                            icon = Icons.Default.Phone
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Provider Information Section
                ModernInfoSection(
                    title = "Thông tin nhà cung cấp",
                    icon = Icons.Default.Store
                ) {
                    ModernInfoRow(
                        label = "Người cung cấp",
                        value = orderDetail.provider?.let { provider ->
                            listOfNotNull(provider.name, provider.username, provider.email).firstOrNull { !it.isNullOrBlank() }
                        } ?: "Chưa tải được thông tin nhà cung cấp",
                        icon = Icons.Default.Store,
                        isHighlight = true
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Location and Payment Section
                ModernInfoSection(
                    title = "Địa điểm & thanh toán",
                    icon = Icons.Default.LocationOn
                ) {
                    ModernInfoRow(
                        label = "Địa chỉ đặt hàng",
                        value = orderDetail.booking.location?.toString()?.replace("\"", "") ?: "Không có thông tin",
                        icon = Icons.Default.LocationOn
                    )
                    ModernInfoRow(
                        label = "Giá thành",
                        value = if (orderDetail.transaction != null) 
                            "${String.format("%.0f", orderDetail.transaction.amount)}đ" 
                        else 
                            "${String.format("%.0f", orderDetail.providerService?.customPrice ?: 0.0)}đ",
                        icon = Icons.Default.AttachMoney,
                        isHighlight = true
                    )
                    orderDetail.transaction?.let { transaction ->
                        ModernInfoRow(
                            label = "Thanh toán",
                            value = "${getPaymentMethodText(transaction.paymentMethod)} - ${getPaymentStatusText(transaction.status)}",
                            icon = Icons.Default.Payment
                        )
                    } ?: ModernInfoRow(
                        label = "Thanh toán",
                        value = "Chưa có thông tin",
                        icon = Icons.Default.Payment
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Time Information Section
                ModernInfoSection(
                    title = "Thời gian thực hiện",
                    icon = Icons.Default.Schedule
                ) {
                    ModernInfoRow(
                        label = "Bắt đầu",
                        value = formatDateString(orderDetail.booking.startAt),
                        icon = Icons.Default.Schedule
                    )
                    ModernInfoRow(
                        label = "Kết thúc",
                        value = formatDateString(orderDetail.booking.endAt),
                        icon = Icons.Default.Schedule
                    )
                }

                // Description (if available)
                orderDetail.booking.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        ModernInfoSection(
                            title = "Mô tả",
                            icon = Icons.Default.Description
                        ) {
                            ModernInfoRow(
                                label = "Chi tiết",
                                value = desc,
                                icon = Icons.Default.Description
                            )
                        }
                    }
                }
                
                // Cancel button (if order can be cancelled)
                val canCancel = orderDetail.booking.status.lowercase() !in listOf("completed", "cancelled")
                if (canCancel) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CancelOrderButton(
                        orderId = orderDetail.booking.id,
                        orderViewModel = orderViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelOrderButton(
    orderId: Long,
    orderViewModel: OrderManagementViewModel
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { showConfirmDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Hủy đơn hàng",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
    }
    
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { 
                Text(
                    text = "Xác nhận hủy đơn hàng",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Text(
                    text = "Bạn có chắc chắn muốn hủy đơn hàng #$orderId không? Hành động này không thể hoàn tác.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        orderViewModel.cancelOrder(orderId)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Hủy đơn hàng",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Đóng",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun StatusChip(status: String) {
    val backgroundColor = when (status.lowercase()) {
        "completed" -> MaterialTheme.colorScheme.primaryContainer
        "cancelled" -> MaterialTheme.colorScheme.errorContainer
        "in_progress" -> MaterialTheme.colorScheme.secondaryContainer
        "confirmed" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (status.lowercase()) {
        "completed" -> MaterialTheme.colorScheme.onPrimaryContainer
        "cancelled" -> MaterialTheme.colorScheme.onErrorContainer
        "in_progress" -> MaterialTheme.colorScheme.onSecondaryContainer
        "confirmed" -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = getStatusText(status),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getStatusText(status: String): String {
    return when (status.lowercase()) {
        "completed" -> "Hoàn thành"
        "cancelled" -> "Đã hủy"
        "in_progress" -> "Đang thực hiện"
        "confirmed" -> "Đã xác nhận"
        "pending" -> "Chờ xác nhận"
        else -> status
    }
}

private fun getPaymentMethodText(method: String): String {
    return when (method.lowercase()) {
        "cash" -> "Tiền mặt"
        "paypal" -> "PayPal"
        "bank_transfer" -> "Chuyển khoản"
        else -> method
    }
}

private fun getPaymentStatusText(status: String): String {
    return when (status.lowercase()) {
        "completed" -> "Đã thanh toán"
        "pending" -> "Chờ thanh toán"
        "failed" -> "Thất bại"
        else -> status
    }
}

@Composable
private fun ModernStatusChip(status: String) {
    val (backgroundColor, contentColor, text) = when (status.lowercase()) {
        "completed" -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "Hoàn thành"
        )
        "cancelled" -> Triple(
            Color(0xFFF44336),
            Color.White,
            "Đã hủy"
        )
        "in_progress" -> Triple(
            Color(0xFFFF9800),
            Color.White,
            "Đang thực hiện"
        )
        "confirmed" -> Triple(
            Color(0xFF2196F3),
            Color.White,
            "Đã xác nhận"
        )
        else -> Triple(
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurface,
            status
        )
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ModernInfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable 
private fun ModernInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}