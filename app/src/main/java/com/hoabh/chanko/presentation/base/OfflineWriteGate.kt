package com.hoabh.chanko.presentation.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cổng kiểm soát thao tác ghi ngoại tuyến (Offline Write Gate):
 * - Khi ngoại tuyến: Cho phép đọc dữ liệu cache bình thường.
 * - Chặn mọi thao tác ghi/đổi trạng thái (Unfreeze, xác nhận mua, hoàn tiền)
 *   và phát thông báo tiếng Việt yêu cầu kết nối mạng.
 * - Tự động mở khóa khi có mạng trở lại.
 */
@Singleton
class OfflineWriteGate @Inject constructor() {
    private val _isOnline = MutableStateFlow(true)
    val isOnline: Flow<Boolean> = _isOnline.asStateFlow()

    fun isNetworkAvailable(): Boolean = _isOnline.value

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }

    inline fun <T> runIfOnline(action: () -> T): Result<T> {
        return if (isNetworkAvailable()) {
            Result.success(action())
        } else {
            Result.failure(OfflineBlockedException("Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thực hiện thao tác này."))
        }
    }
}

class OfflineBlockedException(
    message: String = "Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thực hiện thao tác này."
) : Exception(message)
