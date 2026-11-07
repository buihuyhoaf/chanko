package com.hoabh.chanko.domain.rewards

import com.hoabh.chanko.domain.chat.model.MascotEmotion

/**
 * Sự kiện kinh tế hoặc hành vi ảnh hưởng đến phản ứng cảm xúc của Chanko Mascot.
 */
sealed interface MascotEvent {
    data class RewardEarned(val hatEarned: Long, val pateEarned: Long) : MascotEvent
    data class LevelUp(val newLevel: Int) : MascotEvent
    data class EarlyUnfreeze(val hatDeducted: Long, val pateDeducted: Long) : MascotEvent
    data class PurchaseRecorded(val itemName: String) : MascotEvent
    data object DailyCheckInCompleted : MascotEvent
    data object CoolingOffWaiting : MascotEvent
    data object ItemPassed : MascotEvent
}

/**
 * Phản hồi cảm xúc và thông điệp tương ứng của Chanko Mascot.
 *
 * Tuân thủ quy tắc Mascot Economy (MASCOT-FEEDING = A):
 * - Phản ứng vui vẻ / khích lệ khi nhận thưởng hoặc lên cấp.
 * - Khi Unfreeze sớm: Chanko hoàn toàn trung lập, không phán xét, không khóc, không giận dỗi.
 * - Không có chỉ số đói, bệnh tật hay phán xét hành vi mua sắm.
 */
data class MascotReaction(
    val emotion: MascotEmotion,
    val messageVi: String,
    val isCelebratory: Boolean = false
)

object MascotReactionPolicy {
    fun resolveReaction(event: MascotEvent): MascotReaction {
        return when (event) {
            is MascotEvent.RewardEarned -> MascotReaction(
                emotion = MascotEmotion.ENCOURAGING,
                messageVi = "Bạn đã nhận được +${event.hatEarned} Hạt và +${event.pateEarned} Pate!",
                isCelebratory = true
            )
            is MascotEvent.LevelUp -> MascotReaction(
                emotion = MascotEmotion.WELCOME,
                messageVi = "Chúc mừng! Chanko đã lớn hơn và đạt Cấp ${event.newLevel}!",
                isCelebratory = true
            )
            is MascotEvent.DailyCheckInCompleted -> MascotReaction(
                emotion = MascotEmotion.ENCOURAGING,
                messageVi = "Tuyệt vời! Cảm ơn bạn đã kiên định không chi tiêu ngoài dự kiến hôm nay.",
                isCelebratory = true
            )
            is MascotEvent.EarlyUnfreeze -> MascotReaction(
                emotion = MascotEmotion.NEUTRAL,
                messageVi = "Đã mở sớm món đồ. Đã khấu trừ ${event.hatDeducted} Hạt và ${event.pateDeducted} Pate.",
                isCelebratory = false
            )
            is MascotEvent.PurchaseRecorded -> MascotReaction(
                emotion = MascotEmotion.NEUTRAL,
                messageVi = "Đã ghi nhận giao dịch mua ${event.itemName}.",
                isCelebratory = false
            )
            is MascotEvent.ItemPassed -> MascotReaction(
                emotion = MascotEmotion.RELAXED,
                messageVi = "Bạn đã quyết định dừng lại và bảo toàn ngân sách thành công.",
                isCelebratory = false
            )
            is MascotEvent.CoolingOffWaiting -> MascotReaction(
                emotion = MascotEmotion.RELAXED,
                messageVi = "Thời gian hạ nhiệt đang đếm ngược. Cứ từ từ cân nhắc nhé.",
                isCelebratory = false
            )
        }
    }
}
