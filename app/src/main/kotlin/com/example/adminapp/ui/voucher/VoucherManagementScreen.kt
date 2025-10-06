package com.example.adminapp.ui.voucher

import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminapp.data.model.Voucher
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherManagementScreen(
    onBack: () -> Unit = {},
    viewModel: VoucherManagementViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedVoucher by remember { mutableStateOf<Voucher?>(null) }

    // Load vouchers when screen starts
    LaunchedEffect(Unit) {
        viewModel.loadVouchers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Quản lý khuyến mại",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm voucher",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFE9ECEF)
                        )
                    )
                )
        ) {
            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Đang tải danh sách voucher...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                viewModel.vouchers.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Chưa có voucher nào",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Nhấn nút + để thêm voucher đầu tiên",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.vouchers) { voucher ->
                            VoucherCard(
                                voucher = voucher,
                                onToggleStatus = { viewModel.toggleVoucherStatus(voucher) },
                                onEdit = { selectedVoucher = voucher; showEditDialog = true },
                                onDelete = { selectedVoucher = voucher; showDeleteDialog = true },
                                isUpdating = viewModel.isUpdating
                            )
                        }
                        
                        // Add padding at bottom for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Error handling
    viewModel.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }
    
    // Add Voucher Dialog
    if (showAddDialog) {
        AddVoucherDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, describe, discount, date ->
                viewModel.addVoucher(name, describe, discount, date)
                showAddDialog = false
            },
            isLoading = viewModel.isUpdating
        )
    }
    
    // Edit Voucher Dialog
    if (showEditDialog && selectedVoucher != null) {
        EditVoucherDialog(
            voucher = selectedVoucher!!,
            onDismiss = { 
                showEditDialog = false
                selectedVoucher = null
            },
            onUpdate = { name, describe, discount, date ->
                viewModel.updateVoucher(selectedVoucher!!.id, name, describe, discount, date)
                showEditDialog = false
                selectedVoucher = null
            },
            isLoading = viewModel.isUpdating
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedVoucher != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedVoucher = null
            },
            title = { Text("Xác nhận xóa") },
            text = {
                Text("Bạn có chắc chắn muốn xóa voucher \"${selectedVoucher!!.name}\"? Thao tác này không thể hoàn tác.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVoucher(selectedVoucher!!.id)
                        showDeleteDialog = false
                        selectedVoucher = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedVoucher = null
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun VoucherCard(
    voucher: Voucher,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isUpdating: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voucher Icon
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (voucher.status == "enable") 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = if (voucher.status == "enable") 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Voucher Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = voucher.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Giảm ${voucher.discount}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Status and Options
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Switch
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Switch(
                            checked = voucher.status == "enable",
                            onCheckedChange = { if (!isUpdating) onToggleStatus() },
                            enabled = !isUpdating
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (voucher.status == "enable") "ENABLE" else "DISABLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (voucher.status == "enable") 
                                Color(0xFF4CAF50) 
                            else 
                                Color(0xFFFF5722),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Options Menu
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sửa") },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color.Red) },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                }
                            )
                        }
                    }
                }
            }
            
            // Description
            if (voucher.describe.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = voucher.describe,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Expiry Date
            voucher.date?.let { date ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hết hạn: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun AddVoucherDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Float, String?) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var describe by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Thêm voucher") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên voucher") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = describe,
                    onValueChange = { describe = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Phần trăm giảm giá (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Ngày hết hạn (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    placeholder = { Text("2024-12-31") }
                )
                
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang thêm voucher...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val discountFloat = discount.toFloatOrNull() ?: 0f
                    val trimmedDate = date.trim()
                    val expiryDate = if (trimmedDate.isNotBlank()) trimmedDate else null
                    println("=== UI DEBUG ===")
                    println("Original date: '$date'")
                    println("Trimmed date: '$trimmedDate'") 
                    println("Final expiry date: $expiryDate")
                    onAdd(name, describe, discountFloat, expiryDate)
                },
                enabled = !isLoading && name.isNotBlank() && describe.isNotBlank() && discount.isNotBlank()
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun EditVoucherDialog(
    voucher: Voucher,
    onDismiss: () -> Unit,
    onUpdate: (String, String, Float, String?) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf(voucher.name) }
    var describe by remember { mutableStateOf(voucher.describe) }
    var discount by remember { mutableStateOf(voucher.discount.toString()) }
    var date by remember { mutableStateOf(voucher.date ?: "") }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Sửa voucher") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên voucher") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = describe,
                    onValueChange = { describe = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3
                )
                
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Phần trăm giảm giá (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Ngày hết hạn (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    placeholder = { Text("2024-12-31") }
                )
                
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang cập nhật voucher...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val discountFloat = discount.toFloatOrNull() ?: voucher.discount
                    val trimmedDate = date.trim()
                    val expiryDate = if (trimmedDate.isNotBlank()) trimmedDate else null
                    println("=== EDIT UI DEBUG ===")
                    println("Original date: '$date'")
                    println("Trimmed date: '$trimmedDate'")
                    println("Final expiry date: $expiryDate")
                    onUpdate(name, describe, discountFloat, expiryDate)
                },
                enabled = !isLoading && name.isNotBlank() && describe.isNotBlank() && discount.isNotBlank()
            ) {
                Text("Cập nhật")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Hủy")
            }
        }
    )
}