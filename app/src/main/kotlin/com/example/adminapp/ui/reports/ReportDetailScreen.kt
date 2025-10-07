package com.example.adminapp.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.adminapp.data.model.ReportWithUser
import com.example.adminapp.ui.report.ReportManagementViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportWithUser: ReportWithUser,
    onNavigateBack: () -> Unit,
    viewModel: ReportManagementViewModel = viewModel()
) {
    val isUpdating by viewModel.isUpdating.collectAsState()
    val error by viewModel.error.collectAsState()
    val reports by viewModel.reports.collectAsState()
    var showResponseDialog by remember { mutableStateOf(false) }
    var responseText by remember { mutableStateOf("") }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }
    
    // Get updated report from reports list
    val updatedReport = reports.find { it.report.id == reportWithUser.report.id } ?: reportWithUser
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết báo cáo #${updatedReport.report.id}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (reportWithUser.report.status == "pending") {
                        TextButton(
                            onClick = { showResponseDialog = true }
                        ) {
                            Text("Phản hồi", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            StatusCard(updatedReport.report.status)
            
            // Customer Info Card
            CustomerInfoCard(updatedReport)
            
            // Booking Info Card (if available)
            updatedReport.report.bookingId?.let { bookingId ->
                BookingInfoCard(bookingId)
            }
            
            // Provider Info Card (if available)
            updatedReport.report.providerId?.let { providerId ->
                ProviderInfoCard(
                    providerId = providerId,
                    providerName = updatedReport.providerName
                )
            }
            
            // Report Content Card
            ReportContentCard(updatedReport.report)
            
            // Images Card (if available)
            updatedReport.report.imageUrls?.let { imageUrls ->
                ImagesCard(
                    imageUrls = imageUrls,
                    onImageClick = { url ->
                        selectedImageUrl = url
                        showImageDialog = true
                    }
                )
            }
            
            // Admin Response Card (if available)
            updatedReport.report.adminResponse?.let { response ->
                AdminResponseCard(response, updatedReport.report.resolvedAt)
            }
            
            // Timeline Card
            TimelineCard(updatedReport.report)
        }
    }
    
    // Response Dialog
    if (showResponseDialog) {
        AdminResponseDialog(
            responseText = responseText,
            onResponseChange = { responseText = it },
            onConfirm = {
                viewModel.updateReportStatus(
                    updatedReport.report.id,
                    "completed", // Đổi từ pending thành completed
                    responseText
                )
                showResponseDialog = false
                responseText = ""
            },
            onDismiss = { 
                showResponseDialog = false
                responseText = ""
            }
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
private fun StatusCard(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                "pending" -> MaterialTheme.colorScheme.errorContainer
                "completed" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (status) {
                    "pending" -> "🕐 Chờ xác nhận"
                    "completed" -> "✅ Đã hoàn thành"
                    else -> "❓ $status"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CustomerInfoCard(reportWithUser: ReportWithUser) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Text(
                    text = "Thông tin khách hàng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                if (reportWithUser.customerAvatar != null) {
                    AsyncImage(
                        model = reportWithUser.customerAvatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(24.dp)
                            )
                            .padding(12.dp)
                    )
                }
                
                Column {
                    Text(
                        text = reportWithUser.customerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ID: ${reportWithUser.report.userId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderInfoCard(
    providerId: String,
    providerName: String?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Business, contentDescription = null)
                Text(
                    text = "Nhà cung cấp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Provider ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ID:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = providerId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            // Provider Name (if available)
            providerName?.let { name ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tên:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingInfoCard(bookingId: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Text(
                    text = "Thông tin booking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Booking ID:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "#$bookingId",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReportContentCard(report: com.example.adminapp.data.model.Report) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Text(
                    text = "Nội dung báo cáo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun ImagesCard(
    imageUrls: String,
    onImageClick: (String) -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Parse JSON array from Supabase
            val urls = try {
                if (imageUrls.startsWith("[") && imageUrls.endsWith("]")) {
                    // Handle JSON array format: ["url1","url2","url3"]
                    imageUrls
                        .removePrefix("[")
                        .removeSuffix("]")
                        .split("\",\"")
                        .map { it.replace("\"", "").trim() }
                        .filter { it.isNotEmpty() }
                } else {
                    // Handle simple comma-separated format
                    imageUrls.split(",", ";", "|").map { it.trim() }.filter { it.isNotEmpty() }
                }
            } catch (e: Exception) {
                // Fallback to simple comma splitting if JSON parsing fails
                imageUrls.split(",", ";", "|").map { it.trim() }.filter { it.isNotEmpty() }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Text(
                    text = "Hình ảnh đính kèm (${urls.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (urls.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    urls.forEach { url ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            onClick = { onImageClick(url) }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Report Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Không thể hiển thị hình ảnh",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AdminResponseCard(response: String, resolvedAt: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📝 Phản hồi của Admin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = response,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            resolvedAt?.let {
                Text(
                    text = "Phản hồi lúc: ${formatDateTime(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TimelineCard(report: com.example.adminapp.data.model.Report) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null)
                Text(
                    text = "Thời gian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.createdAt?.let {
                    TimelineItem("Tạo báo cáo", formatDateTime(it))
                }
                
                report.updatedAt?.let {
                    TimelineItem("Cập nhật", formatDateTime(it))
                }
                
                report.resolvedAt?.let {
                    TimelineItem("Phản hồi", formatDateTime(it))
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(label: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminResponseDialog(
    responseText: String,
    onResponseChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gửi phản hồi") },
        text = {
            Column {
                Text("Nhập phản hồi cho báo cáo này:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = responseText,
                    onValueChange = onResponseChange,
                    placeholder = { Text("Nhập phản hồi...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = responseText.isNotBlank()
            ) {
                Text("Gửi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

// Utility function to format date
private fun formatDateTime(dateTimeString: String?): String {
    if (dateTimeString.isNullOrEmpty()) return "N/A"
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        // Try alternative format if first fails
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = isoFormat.parse(dateTimeString)
            outputFormat.format(date ?: Date())
        } catch (e2: Exception) {
            dateTimeString // Return original if can't parse
        }
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
                    model = imageUrl,
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