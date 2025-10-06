package com.example.adminapp.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminapp.data.model.DailyOrders
import com.example.adminapp.data.model.DailyRevenue
import com.example.adminapp.data.model.StatisticsData
import com.example.adminapp.data.model.TransactionSummary
import kotlin.math.max
import kotlin.math.min
import java.text.SimpleDateFormat
import java.util.*
import java.text.NumberFormat
import java.util.Locale

// Helper function to format currency in Vietnamese style
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return formatter.format(amount.toLong()) + "đ"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val statisticsData = viewModel.statisticsData
    val isLoading = viewModel.isLoading
    val error = viewModel.error

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê báo cáo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStatistics() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới"
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
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadStatistics() }) {
                            Text("Thử lại")
                        }
                    }
                }
                statisticsData != null -> {
                    StatisticsContent(statisticsData)
                }
                else -> {
                    Text(
                        text = "Không có dữ liệu thống kê",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsContent(data: StatisticsData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Summary Cards
        item {
            SummaryCards(data)
        }
        
        // Revenue Chart
        item {
            RevenueChart(data.dailyRevenues)
        }
        
        // Orders Chart  
        item {
            OrdersChart(data.dailyOrders)
        }
        
        // Transactions Table
        item {
            TransactionsTable(data.transactionSummaries)
        }
    }
}

@Composable
private fun SummaryCards(data: StatisticsData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tổng doanh thu",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = formatCurrency(data.totalRevenue),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Đơn hoàn thành",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "${data.totalCompletedOrders}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RevenueChart(dailyRevenues: List<DailyRevenue>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Biểu đồ doanh thu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Legend và tổng doanh thu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(color = Color(0xFF4CAF50), label = "PayPal")
                    LegendItem(color = Color(0xFFFF5722), label = "Tiền mặt")
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val totalPaypal = dailyRevenues.sumOf { it.paypalRevenue }
                    val totalCash = dailyRevenues.sumOf { it.cashRevenue }
                    
                    Text(
                        text = "PayPal: ${formatCurrency(totalPaypal)}",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tiền mặt: ${formatCurrency(totalCash)}",
                        fontSize = 10.sp,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dailyRevenues.isNotEmpty()) {
                LineChart(
                    data = dailyRevenues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có dữ liệu doanh thu")
                }
            }
        }
    }
}

@Composable
private fun OrdersChart(dailyOrders: List<DailyOrders>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Biểu đồ đơn hàng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dailyOrders.isNotEmpty()) {
                BarChart(
                    data = dailyOrders,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có dữ liệu đơn hàng")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionsTable(transactions: List<TransactionSummary>) {
    var currentPage by remember { mutableStateOf(0) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionSummary?>(null) }
    
    val itemsPerPage = 15
    val totalPages = (transactions.size + itemsPerPage - 1) / itemsPerPage
    val startIndex = currentPage * itemsPerPage
    val endIndex = minOf(startIndex + itemsPerPage, transactions.size)
    val currentTransactions = transactions.subList(startIndex, endIndex)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                    text = "Bảng giao dịch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Trang ${currentPage + 1}/$totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Booking ID",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Hình thức",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Số tiền",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Doanh thu",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Table Rows
            currentTransactions.forEach { transaction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onLongClick = {
                                selectedTransaction = transaction
                                showDetailDialog = true
                            },
                            onClick = { }
                        )
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = transaction.bookingId.toString(),
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = when (transaction.paymentMethod.lowercase()) {
                            "paypal" -> "PayPal"
                            "tiền mặt" -> "Tiền mặt"
                            else -> transaction.paymentMethod
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatCurrency(transaction.amount),
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = formatCurrency(transaction.revenue),
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        color = Color(0xFF4CAF50)
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))
            }
            
            // Pagination Controls
            if (totalPages > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0
                    ) {
                        Text("Trang trước")
                    }
                    
                    Text(
                        text = "${startIndex + 1}-${endIndex} / ${transactions.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    TextButton(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Text("Trang sau")
                    }
                }
            }
        }
    }
    
    // Detail Dialog
    if (showDetailDialog && selectedTransaction != null) {
        AlertDialog(
            onDismissRequest = { 
                showDetailDialog = false 
                selectedTransaction = null
            },
            title = { Text("Chi tiết giao dịch") },
            text = {
                Column {
                    selectedTransaction?.let { transaction ->
                        // Format date
                        val dateText = try {
                            if (!transaction.createdAt.isNullOrEmpty()) {
                                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val date = inputFormat.parse(transaction.createdAt)
                                outputFormat.format(date ?: Date())
                            } else {
                                "Không có thông tin"
                            }
                        } catch (e: Exception) {
                            "Không có thông tin"
                        }
                        
                        Text("Ngày: $dateText")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Số đơn hàng: ${transaction.bookingId}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hình thức thanh toán: ${
                            when (transaction.paymentMethod.lowercase()) {
                                "paypal" -> "PayPal"
                                "tiền mặt" -> "Tiền mặt"
                                else -> transaction.paymentMethod
                            }
                        }")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Số tiền: ${formatCurrency(transaction.amount)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Doanh thu: ${formatCurrency(transaction.revenue)}",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showDetailDialog = false
                    selectedTransaction = null
                }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LineChart(
    data: List<DailyRevenue>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 60f
        
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding - 40f // Extra space for dates
        
        val maxRevenue = max(
            data.maxOfOrNull { it.paypalRevenue } ?: 0.0,
            data.maxOfOrNull { it.cashRevenue } ?: 0.0
        ).toFloat()
        
        if (maxRevenue <= 0) return@Canvas
        
        // Draw PayPal line (green)
        val paypalPath = Path()
        data.forEachIndexed { index, item ->
            val x = padding + (index * chartWidth / (data.size - 1).coerceAtLeast(1))
            val y = padding + chartHeight - (item.paypalRevenue.toFloat() / maxRevenue * chartHeight)
            
            if (index == 0) {
                paypalPath.moveTo(x, y)
            } else {
                paypalPath.lineTo(x, y)
            }
            
            // Draw points
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        drawPath(
            path = paypalPath,
            color = Color(0xFF4CAF50),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw Cash line (red)
        val cashPath = Path()
        data.forEachIndexed { index, item ->
            val x = padding + (index * chartWidth / (data.size - 1).coerceAtLeast(1))
            val y = padding + chartHeight - (item.cashRevenue.toFloat() / maxRevenue * chartHeight)
            
            if (index == 0) {
                cashPath.moveTo(x, y)
            } else {
                cashPath.lineTo(x, y)
            }
            
            // Draw points
            drawCircle(
                color = Color(0xFFFF5722),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        drawPath(
            path = cashPath,
            color = Color(0xFFFF5722),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw date labels
        data.forEachIndexed { index, item ->
            val x = padding + (index * chartWidth / (data.size - 1).coerceAtLeast(1))
            val dateLabel = try {
                val parts = item.date.split("-")
                "${parts[2]}/${parts[1]}" // DD/MM format
            } catch (e: Exception) {
                item.date
            }
            
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    dateLabel,
                    x,
                    height - 10f,
                    paint
                )
            }
        }
    }
}

@Composable
private fun BarChart(
    data: List<DailyOrders>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 60f
        
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding - 60f // Extra space for labels
        
        val maxOrders = (data.maxOfOrNull { it.completedOrders } ?: 0).toFloat()
        
        if (maxOrders <= 0) return@Canvas
        
        val barWidth = chartWidth / data.size * 0.6f
        
        data.forEachIndexed { index, item ->
            val barHeight = (item.completedOrders.toFloat() / maxOrders) * chartHeight
            val x = padding + (index * chartWidth / data.size) + (chartWidth / data.size - barWidth) / 2
            val y = padding + chartHeight - barHeight
            val centerX = x + barWidth / 2
            
            // Draw bar
            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
            
            // Draw order count on top of bar
            if (item.completedOrders > 0) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawText(
                        item.completedOrders.toString(),
                        centerX,
                        y - 10f,
                        paint
                    )
                }
            }
            
            // Draw date below bar
            val dateLabel = try {
                val parts = item.date.split("-")
                "${parts[2]}/${parts[1]}" // DD/MM format
            } catch (e: Exception) {
                item.date
            }
            
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    dateLabel,
                    centerX,
                    height - 10f,
                    paint
                )
            }
        }
    }
}