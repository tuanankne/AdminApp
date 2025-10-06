package com.example.adminapp.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.adminapp.data.model.ReportWithUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportWithUser: ReportWithUser,
    onBack: () -> Unit,
    onStatusUpdate: (String, String?) -> Unit = { _, _ -> },
    viewModel: ReportManagementViewModel = viewModel()
) {
    var showResponseDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }
    
    val report = reportWithUser.report
    val imageUrls = report.imageUrls?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Chi tiết báo cáo #${report.id}",
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
                actions = {
                    IconButton(onClick = { showResponseDialog = true }) {
                        Icon(
                            Icons.Default.Reply,
                            contentDescription = "Phản hồi",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
            // Customer Info Section
            item {
                CustomerInfoCard(reportWithUser = reportWithUser)
            }
            
            // Report Status Section
            item {
                StatusCard(report = report, onStatusUpdate = onStatusUpdate)
            }
            
            // Report Content Section
            item {
                ReportContentCard(report = report)
            }
            
            // Images Section (if any)
            if (imageUrls.isNotEmpty()) {
                item {
                    ImagesCard(
                        imageUrls = imageUrls,
                        onImageClick = { url ->
                            selectedImageUrl = url
                            showImageDialog = true
                        }
                    )
                }
            }
            
            // Booking Info Section (if exists)
            if (report.bookingId != null) {
                item {
                    BookingInfoCard(bookingId = report.bookingId)
                }
            }
            
            // Provider Info Section (if exists) 
            if (report.providerId != null) {
                item {
                    ProviderInfoCard(providerId = report.providerId)
                }
            }
            
            // Admin Response Section
            item {
                AdminResponseCard(report = report)
            }
            
            // Timeline Section
            item {
                TimelineCard(report = report)
            }
        }
    }
    
    // Response Dialog
    if (showResponseDialog) {
        AdminResponseDialog(
            report = reportWithUser,
            onDismiss = { showResponseDialog = false },
            onSubmit = { response ->
                onStatusUpdate("processing", response)
                showResponseDialog = false
            },
            isLoading = false
        )
    }
    
    // Image Viewer Dialog
    if (showImageDialog) {
        ImageViewerDialog(
            imageUrl = selectedImageUrl,
            onDismiss = { showImageDialog = false }
        )
    }
}

@Composable
private fun CustomerInfoCard(reportWithUser: ReportWithUser) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
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
                        contentDescription = "Avatar",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Khách hàng",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = reportWithUser.customerName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "ID: ${reportWithUser.report.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatusCard(report: com.example.adminapp.data.model.Report, onStatusUpdate: (String, String?) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trạng thái",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                StatusBadge(status = report.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (report.status != "resolved") {
                    OutlinedButton(
                        onClick = { onStatusUpdate("resolved", null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đã giải quyết")
                    }
                }
                
                if (report.status == "pending") {
                    Button(
                        onClick = { onStatusUpdate("processing", null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bắt đầu xử lý")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportContentCard(report: com.example.adminapp.data.model.Report) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nội dung báo cáo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = report.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun ImagesCard(
    imageUrls: List<String>,
    onImageClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hình ảnh đính kèm (${imageUrls.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUrls) { imageUrl ->
                    Card(
                        modifier = Modifier
                            .size(100.dp),
                        onClick = { onImageClick(imageUrl) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Report Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingInfoCard(bookingId: Long) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thông tin đặt lịch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Event, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Booking ID: $bookingId")
            }
        }
    }
}

@Composable
private fun ProviderInfoCard(providerId: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thông tin nhà cung cấp",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Business, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Provider ID: $providerId")
            }
        }
    }
}

@Composable
private fun AdminResponseCard(report: com.example.adminapp.data.model.Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (report.adminResponse != null) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (report.adminResponse != null) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = if (report.adminResponse != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Phản hồi Admin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = report.adminResponse ?: "Chưa có phản hồi từ admin",
                style = MaterialTheme.typography.bodyLarge,
                color = if (report.adminResponse != null) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun TimelineCard(report: com.example.adminapp.data.model.Report) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thời gian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Created
            TimelineItem(
                icon = Icons.Default.Create,
                title = "Tạo báo cáo",
                time = report.createdAt ?: "N/A",
                isCompleted = true
            )
            
            // Updated
            if (report.updatedAt != null) {
                TimelineItem(
                    icon = Icons.Default.Update,
                    title = "Cập nhật lần cuối",
                    time = report.updatedAt,
                    isCompleted = true
                )
            }
            
            // Resolved
            if (report.resolvedAt != null) {
                TimelineItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Đã giải quyết",
                    time = report.resolvedAt,
                    isCompleted = true
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    time: String,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White
                    )
                }
            }
        }
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