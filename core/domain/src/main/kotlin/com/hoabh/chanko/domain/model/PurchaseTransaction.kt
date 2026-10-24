package com.hoabh.chanko.domain.model

enum class PaymentType {
    PAY_NOW,
    CREDIT
}

data class PurchaseTransaction(
    val id: String,
    val itemId: Int,
    val actualUnitPrice: Long,
    val quantity: Int = 1,
    val totalAmount: Long = actualUnitPrice * quantity,
    val paymentType: PaymentType = PaymentType.PAY_NOW,
    val cardLabel: String? = null,
    val purchasedTimestamp: Long,
    val note: String = ""
)
