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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: OrderListViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOrderClick: (Long) -> Unit = {}
) {
    val orders = viewModel.orders
    val isLoading = viewModel.isLoading
    val error = viewModel.error
    var searchId by remember { mutableStateOf("") }
    var showSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
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
}    // Search Dialog
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
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Xem chi tiết",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}