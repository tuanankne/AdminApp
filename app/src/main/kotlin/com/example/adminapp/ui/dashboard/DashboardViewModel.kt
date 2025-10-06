package com.example.adminapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    
    private val reportRepository = ReportRepository()
    
    private val _pendingReportsCount = MutableStateFlow(0)
    val pendingReportsCount: StateFlow<Int> = _pendingReportsCount
    
    init {
        loadPendingReportsCount()
    }
    
    fun loadPendingReportsCount() {
        viewModelScope.launch {
            try {
                val count = reportRepository.getPendingReportsCount()
                _pendingReportsCount.value = count
                println("Dashboard: Pending reports count updated to $count")
            } catch (e: Exception) {
                println("Error loading pending reports count: ${e.message}")
                _pendingReportsCount.value = 0
            }
        }
    }
    
    fun refreshData() {
        loadPendingReportsCount()
    }
}