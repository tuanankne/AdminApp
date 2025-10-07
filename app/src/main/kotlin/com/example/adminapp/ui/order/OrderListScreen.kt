package com.example.adminapp.ui.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminapp.ui.order.OrderStatistics

@Composable
private fun OrderStatisticsCard(statistics: OrderStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Tổng số đơn hàng
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statistics.totalOrders.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Tổng đơn hàng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Đơn hàng hoàn thành
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statistics.completedOrders.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Đã hoàn thành",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

private fun getStatusText(status: String): String {
    return when (status.lowercase()) {
        "pending" -> "Đang chờ"
        "confirmed" -> "Đã xác nhận"
        "in_progress" -> "Đang thực hiện"
        "completed" -> "Hoàn thành"
        "cancelled" -> "Đã hủy"
        "rejected" -> "Từ chối"
        else -> status
    }
}

@Composable
private fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
    return when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.primary
        "confirmed" -> MaterialTheme.colorScheme.tertiary
        "in_progress" -> MaterialTheme.colorScheme.secondary
        "completed" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        "cancelled" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
        "rejected" -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun OrderListItem(
    orderItem: OrderListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Đơn hàng #${orderItem.bookingId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Người đặt: ${orderItem.customerName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trạng thái: ${getStatusText(orderItem.status)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getStatusColor(orderItem.status)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Xem chi tiết",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    providerId: String? = null,
    viewModel: OrderListViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOrderClick: (Long) -> Unit = {}
) {
    val orders = viewModel.orders
    val statistics = viewModel.statistics
    val isLoading = viewModel.isLoading
    val error = viewModel.error
    var searchId by remember { mutableStateOf("") }
    var showSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(providerId) {
        if (providerId != null) {
            viewModel.loadOrdersByProvider(providerId)
        } else {
            viewModel.loadOrders()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm đơn hàng"
                        )
                    }
                }
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
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadOrders() }) {
                            Text("Thử lại")
                        }
                    }
                }

                orders.isEmpty() -> {
                    Text(
                        text = "Chưa có đơn hàng nào",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Thống kê đơn hàng
                        item {
                            OrderStatisticsCard(statistics = statistics)
                        }

                        // Danh sách đơn hàng
                        items(orders) { orderItem ->
                            OrderListItem(
                                orderItem = orderItem,
                                onClick = { onOrderClick(orderItem.bookingId) }
                            )
                        }
                    }
                }
            }
        }

        // Search Dialog
        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Tìm kiếm đơn hàng") },
                text = {
                    OutlinedTextField(
                        value = searchId,
                        onValueChange = { searchId = it },
                        label = { Text("Nhập ID đơn hàng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            searchId.toLongOrNull()?.let { id ->
                                onOrderClick(id)
                            }
                            showSearchDialog = false
                            searchId = ""
                        },
                        enabled = searchId.isNotBlank()
                    ) {
                        Text("Tìm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSearchDialog = false
                        searchId = ""
                    }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}
