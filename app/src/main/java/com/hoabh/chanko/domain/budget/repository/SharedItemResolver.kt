package com.hoabh.chanko.domain.budget.repository

/**
 * Cầu nối từ Budget sang màn shared Item Detail hiện có (C01, AC13).
 * Trả về ID route (Int) nếu dữ liệu món đồ khả dụng; null nếu không mở được,
 * khi đó UI phải báo "Món đồ chưa có dữ liệu ngoại tuyến" thay vì mở trang trống.
 */
interface SharedItemResolver {
    suspend fun resolveRouteId(itemId: String): Int?
}
