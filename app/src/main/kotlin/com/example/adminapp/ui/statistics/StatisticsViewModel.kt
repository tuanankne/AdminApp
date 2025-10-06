package com.example.adminapp.ui.statistics

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.StatisticsData
import com.example.adminapp.data.repository.StatisticsRepository
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {
    private val statisticsRepository = StatisticsRepository()
    
    var statisticsData by mutableStateOf<StatisticsData?>(null)
        private set
        
    var isLoading by mutableStateOf(false)
        private set
        
    var error by mutableStateOf<String?>(null)
        private set
    
    init {
        loadStatistics()
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                
                statisticsData = statisticsRepository.getStatistics()
                
            } catch (e: Exception) {
                error = "Lỗi khi tải dữ liệu thống kê: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun refreshStatistics() {
        loadStatistics()
    }
}