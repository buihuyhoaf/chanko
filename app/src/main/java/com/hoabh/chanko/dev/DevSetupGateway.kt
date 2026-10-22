package com.hoabh.chanko.dev

import com.hoabh.chanko.presentation.screens.setup.components.SetupDraft
import com.hoabh.chanko.presentation.screens.setup.components.SetupGateway
import com.hoabh.chanko.presentation.screens.setup.components.SetupStatus
import com.hoabh.chanko.presentation.screens.setup.components.SetupSubmission

/**
 * Gateway giả lập cho màn hình Thiết lập ban đầu (Setup) ở môi trường dev.
 * Giúp lập trình viên có thể kiểm thử toàn bộ luồng nhập ngân sách & ngày bắt đầu chu kỳ
 * mà không bị chặn bởi backend chưa cấu hình.
 */
object DevSetupGateway : SetupGateway {
    var isCompleted: Boolean = false

    override suspend fun checkStatus(): SetupStatus {
        return if (isCompleted) SetupStatus.COMPLETE else SetupStatus.INCOMPLETE
    }

    override suspend fun submit(draft: SetupDraft): SetupSubmission {
        isCompleted = true
        return SetupSubmission.ACCEPTED
    }

    fun reset() {
        isCompleted = false
    }
}
