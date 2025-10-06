package com.example.adminapp.ui.voucher

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adminapp.data.model.Voucher
import com.example.adminapp.data.repository.VoucherRepository
import kotlinx.coroutines.launch

class VoucherManagementViewModel : ViewModel() {
    
    private val repository = VoucherRepository()
    
    var vouchers by mutableStateOf<List<Voucher>>(emptyList())
        private set
        
    var isLoading by mutableStateOf(false)
        private set
        
    var error by mutableStateOf<String?>(null)
        private set
        
    var isUpdating by mutableStateOf(false)
        private set

    init {
        loadVouchers()
    }
    
    fun loadVouchers() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                vouchers = repository.getVouchers()
            } catch (e: Exception) {
                error = "Lỗi tải danh sách voucher: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun toggleVoucherStatus(voucher: Voucher) {
        viewModelScope.launch {
            isUpdating = true
            try {
                val newStatus = if (voucher.status == "enable") "disable" else "enable"
                val success = repository.updateVoucherStatus(voucher.id, newStatus)
                if (success) {
                    loadVouchers() // Refresh list
                } else {
                    error = "Không thể cập nhật trạng thái voucher"
                }
            } catch (e: Exception) {
                error = "Lỗi cập nhật trạng thái: ${e.message}"
            } finally {
                isUpdating = false
            }
        }
    }
    
    fun addVoucher(
        name: String,
        describe: String,
        discount: Float,
        date: String? = null
    ) {
        viewModelScope.launch {
            isUpdating = true
            error = null
            try {
                val success = repository.addVoucher(name, describe, discount, date)
                if (success) {
                    loadVouchers() // Refresh list
                } else {
                    error = "Không thể thêm voucher"
                }
            } catch (e: Exception) {
                error = "Lỗi thêm voucher: ${e.message}"
            } finally {
                isUpdating = false
            }
        }
    }
    
    fun updateVoucher(
        id: Long,
        name: String,
        describe: String,
        discount: Float,
        date: String? = null
    ) {
        viewModelScope.launch {
            isUpdating = true
            error = null
            try {
                val success = repository.updateVoucher(id, name, describe, discount, date)
                if (success) {
                    loadVouchers() // Refresh list
                } else {
                    error = "Không thể cập nhật voucher"
                }
            } catch (e: Exception) {
                error = "Lỗi cập nhật voucher: ${e.message}"
            } finally {
                isUpdating = false
            }
        }
    }
    
    fun deleteVoucher(id: Long) {
        viewModelScope.launch {
            isUpdating = true
            error = null
            try {
                val success = repository.deleteVoucher(id)
                if (success) {
                    loadVouchers() // Refresh list
                } else {
                    error = "Không thể xóa voucher"
                }
            } catch (e: Exception) {
                error = "Lỗi xóa voucher: ${e.message}"
            } finally {
                isUpdating = false
            }
        }
    }
    
    fun clearError() {
        error = null
    }
}