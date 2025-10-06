package com.example.adminapp.ui.report

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.ReportWithUser
import com.example.adminapp.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportManagementViewModel : ViewModel() {
    
    private val repository = ReportRepository()
    
    private val _reports = MutableStateFlow<List<ReportWithUser>>(emptyList())
    val reports: StateFlow<List<ReportWithUser>> = _reports
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount
    
    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Test connection first
                println("Testing database connection...")
                val canConnect = repository.testConnection()
                if (!canConnect) {
                    _error.value = "Không thể kết nối đến database reports"
                    return@launch
                }
                
                val reportList = repository.getReports()
                _reports.value = reportList
                println("Final reports loaded: ${reportList.size}")
                
                // Load pending count
                loadPendingCount()
            } catch (e: Exception) {
                println("Exception in loadReports: ${e.message}")
                e.printStackTrace()
                _error.value = "Lỗi tải báo cáo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateReportStatus(reportId: Long, status: String, adminResponse: String? = null) {
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null
            try {
                val success = repository.updateReportStatus(reportId, status, adminResponse)
                if (success) {
                    loadReports() // Refresh list
                } else {
                    _error.value = "Không thể cập nhật trạng thái báo cáo"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi cập nhật báo cáo: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }
    
    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null
            try {
                val success = repository.deleteReport(reportId)
                if (success) {
                    loadReports() // Refresh list
                } else {
                    _error.value = "Không thể xóa báo cáo"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi xóa báo cáo: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }
    
    fun loadPendingCount() {
        viewModelScope.launch {
            try {
                val count = repository.getPendingReportsCount()
                _pendingCount.value = count
                println("Pending reports count: $count")
            } catch (e: Exception) {
                println("Error loading pending count: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}