package com.example.adminapp.ui.report

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.adminapp.data.model.ReportWithUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportManagementScreen(
    onBack: () -> Unit = {},
    onReportClick: (ReportWithUser) -> Unit = {},
    viewModel: ReportManagementViewModel = viewModel()
) {
    var showResponseDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<ReportWithUser?>(null) }
    
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    
    // Load reports when screen starts
    LaunchedEffect(Unit) {
        viewModel.loadReports()
    }
    
    // Show error message
    LaunchedEffect(error) {
        // Handle error display if needed
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Quản lý báo cáo",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Đang tải báo cáo...")
                        }
                    }
                }
                
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    viewModel.clearError()
                                    viewModel.loadReports()
                                }
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                
                reports.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Report,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có báo cáo nào",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Danh sách báo cáo (${reports.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(reports, key = { it.report.id }) { reportWithUser ->
                            ReportCard(
                                reportWithUser = reportWithUser,
                                onClick = {
                                    onReportClick(reportWithUser)
                                },
                                onStatusChange = { status, adminResponse ->
                                    viewModel.updateReportStatus(
                                        reportWithUser.report.id, 
                                        status, 
                                        adminResponse
                                    )
                                },
                                onDelete = {
                                    selectedReport = reportWithUser
                                    showDeleteDialog = true
                                },
                                onResponse = {
                                    selectedReport = reportWithUser
                                    showResponseDialog = true
                                },
                                isUpdating = isUpdating
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Admin Response Dialog
    if (showResponseDialog && selectedReport != null) {
        AdminResponseDialog(
            report = selectedReport!!,
            onDismiss = { 
                showResponseDialog = false
                selectedReport = null
            },
            onSubmit = { response ->
                viewModel.updateReportStatus(
                    selectedReport!!.report.id,
                    "completed",
                    response
                )
                showResponseDialog = false
                selectedReport = null
            },
            isLoading = isUpdating
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedReport != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { 
                Text("Bạn có chắc chắn muốn xóa báo cáo \"${selectedReport!!.report.title}\"?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReport(selectedReport!!.report.id)
                        showDeleteDialog = false
                        selectedReport = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // Report Detail Dialog
    if (showDetailDialog && selectedReport != null) {
        ReportDetailDialog(
            report = selectedReport!!,
            onDismiss = { 
                showDetailDialog = false
                selectedReport = null
            }
        )
    }
    
    // Report Detail Dialog
    if (showDetailDialog && selectedReport != null) {
        ReportDetailDialog(
            report = selectedReport!!,
            onDismiss = { 
                showDetailDialog = false
                selectedReport = null
            }
        )
    }
}

@Composable
private fun ReportCard(
    reportWithUser: ReportWithUser,
    onClick: () -> Unit,
    onStatusChange: (String, String?) -> Unit,
    onDelete: () -> Unit,
    onResponse: () -> Unit,
    isUpdating: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    val report = reportWithUser.report
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header với user info và menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!reportWithUser.customerAvatar.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(reportWithUser.customerAvatar)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ID: ${report.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        report.bookingId?.let { bookingId ->
                            Text(
                                text = "• Booking: #$bookingId",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Text(
                        text = reportWithUser.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Status Badge
                StatusBadge(status = report.status)
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        enabled = !isUpdating
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (report.status != "completed") {
                            DropdownMenuItem(
                                text = { Text("Phản hồi & Hoàn thành") },
                                onClick = {
                                    onResponse()
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Đánh dấu hoàn thành") },
                                onClick = {
                                    onStatusChange("completed", null)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Xóa", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                ) 
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Report Title
            Text(
                text = report.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer with created date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tạo: ${report.createdAt ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                if (report.adminResponse != null) {
                    Text(
                        text = "Đã phản hồi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "pending" -> Color(0xFFFF9800) to "Chờ xử lý"
        "processing" -> Color(0xFF2196F3) to "Đang xử lý"
        "completed" -> Color(0xFF4CAF50) to "Đã hoàn thành"
        "closed" -> Color(0xFF607D8B) to "Đã đóng"
        else -> Color(0xFF757575) to status
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AdminResponseDialog(
    report: ReportWithUser,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    isLoading: Boolean
) {
    var response by remember { mutableStateOf(report.report.adminResponse ?: "") }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Phản hồi báo cáo") },
        text = {
            Column {
                Text(
                    text = "Báo cáo: ${report.report.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = response,
                    onValueChange = { response = it },
                    label = { Text("Phản hồi của admin") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(response) },
                enabled = !isLoading && response.isNotBlank()
            ) {
                Text("Gửi phản hồi")
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
private fun ReportDetailDialog(
    report: ReportWithUser,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Chi tiết báo cáo #${report.report.id}",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    // Customer Info
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Thông tin khách hàng",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (report.customerAvatar != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(report.customerAvatar)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Default Avatar",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = report.customerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "ID: ${report.report.userId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Report Details
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Thông tin báo cáo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            DetailRow("Tiêu đề", report.report.title)
                            DetailRow("Mô tả", report.report.description)
                            
                            if (report.report.bookingId != null) {
                                DetailRow("Booking ID", report.report.bookingId.toString())
                            }
                            
                            if (report.report.providerId != null) {
                                DetailRow("Provider ID", report.report.providerId)
                            }
                            
                            DetailRow("Trạng thái", report.report.status)
                            
                            if (report.report.createdAt != null) {
                                DetailRow("Ngày tạo", report.report.createdAt)
                            }
                            
                            if (report.report.updatedAt != null) {
                                DetailRow("Cập nhật lần cuối", report.report.updatedAt)
                            }
                            
                            if (report.report.imageUrls != null) {
                                DetailRow("Hình ảnh", "Có đính kèm")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.3f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.7f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}