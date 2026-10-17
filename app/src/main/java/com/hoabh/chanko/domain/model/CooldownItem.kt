package com.hoabh.chanko.domain.model


enum class ItemStatus {
    COOLING_DOWN,
    BOUGHT,
    PASSED
}

data class CooldownItem(
    val id: Int = 0,
    val name: String,
    val price: Long,
    val imageUri: String? = null,
    val addedTimestamp: Long,
    val cooldownDurationMillis: Long,
    val reason: String,
    val status: ItemStatus = ItemStatus.COOLING_DOWN
) {
    fun remainingMillis(now: Long): Long =
        (cooldownDurationMillis - (now - addedTimestamp)).coerceIn(0, cooldownDurationMillis)

    fun canDecide(now: Long): Boolean =
        status == ItemStatus.COOLING_DOWN && remainingMillis(now) == 0L
}
