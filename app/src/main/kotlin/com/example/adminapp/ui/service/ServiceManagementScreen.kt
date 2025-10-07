package com.example.adminapp.ui.service

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.adminapp.data.model.ServiceType
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceManagementScreen(
    viewModel: ServiceManagementViewModel = viewModel(),
    onBack: () -> Unit = {},
    onServiceTypeClick: (Long, String) -> Unit = { _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedServiceType by remember { mutableStateOf<ServiceType?>(null) }
    val context = LocalContext.current
    
    // Khởi tạo storage buckets khi screen được load
    LaunchedEffect(Unit) {
        viewModel.initializeStorageBuckets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý dịch vụ") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm loại dịch vụ",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                viewModel.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            viewModel.clearError()
                            viewModel.loadServiceTypes()
                        }) {
                            Text("Thử lại")
                        }
                    }
                }
                viewModel.serviceTypes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có loại dịch vụ nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nhấn nút + để thêm loại dịch vụ mới",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.createSampleData() },
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Tạo dữ liệu mẫu")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.serviceTypes) { serviceType ->
                            ServiceTypeCard(
                                serviceType = serviceType,
                                onToggleStatus = { id, status ->
                                    viewModel.toggleServiceTypeStatus(id, status)
                                },
                                isUpdating = viewModel.isUpdatingStatus,
                                onClick = { onServiceTypeClick(serviceType.id, serviceType.name) },
                                onDelete = { showDeleteDialog = true; selectedServiceType = serviceType }
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
    
    if (showAddDialog) {
        AddServiceTypeDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, description, imageStream, fileName ->
                viewModel.addServiceType(name, description, imageStream, fileName)
                showAddDialog = false
            },
            isLoading = viewModel.isAddingService || viewModel.isUploadingImage,
            context = context
        )
    }
    
    if (showDeleteDialog && selectedServiceType != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedServiceType = null
            },
            title = { Text("Xác nhận xóa") },
            text = { 
                Text("Bạn có chắc chắn muốn xóa loại dịch vụ \"${selectedServiceType!!.name}\"?\n\nLưu ý: Tất cả dịch vụ thuộc loại này cũng sẽ bị xóa.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteServiceType(selectedServiceType!!.id)
                        showDeleteDialog = false
                        selectedServiceType = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    selectedServiceType = null
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}


@Composable
private fun ServiceTypeCard(
    serviceType: ServiceType,
    onToggleStatus: (Long, Boolean) -> Unit,
    isUpdating: Boolean,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Icon
            Card(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (!serviceType.iconUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = serviceType.iconUrl,
                        contentDescription = serviceType.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Service Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "ID: ${serviceType.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = serviceType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!serviceType.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = serviceType.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Status Toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Switch(
                    checked = serviceType.isActive,
                    onCheckedChange = { 
                        if (!isUpdating) {
                            onToggleStatus(serviceType.id, serviceType.isActive)
                        }
                    },
                    enabled = !isUpdating
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (serviceType.isActive) "ON" else "OFF",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (serviceType.isActive) Color(0xFF4CAF50) else Color(0xFFFF5722),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Options Menu
            var showMenu by remember { mutableStateOf(false) }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Xóa") },
                        onClick = {
                            showMenu = false
                            onDelete()
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
    }
}

@Composable
private fun AddServiceTypeDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, InputStream?, String?) -> Unit,
    isLoading: Boolean,
    context: Context
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Thêm loại dịch vụ") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên dịch vụ") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3
                )
                
                // Image Selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    onClick = { 
                        if (!isLoading) {
                            imagePickerLauncher.launch("image/*")
                        }
                    }
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Chọn hình ảnh",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đang thêm dịch vụ...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val inputStream = selectedImageUri?.let { uri ->
                        try {
                            context.contentResolver.openInputStream(uri)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    val fileName = selectedImageUri?.let { uri ->
                        val uuid1 = java.util.UUID.randomUUID().toString()
                        val uuid2 = java.util.UUID.randomUUID().toString()
                        "${uuid1}_${uuid2}.jpg"
                    }
                    onAdd(name, description, inputStream, fileName)
                },
                enabled = !isLoading && name.isNotBlank()
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