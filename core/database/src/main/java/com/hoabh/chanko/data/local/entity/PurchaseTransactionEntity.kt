package com.hoabh.chanko.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hoabh.chanko.domain.model.PaymentType
import com.hoabh.chanko.domain.model.PurchaseTransaction

@Entity(
    tableName = "purchase_transactions",
    indices = [Index(value = ["itemId"])]
)
data class PurchaseTransactionEntity(
    @PrimaryKey val id: String,
    val itemId: Int,
    val actualUnitPrice: Long,
    val quantity: Int,
    val totalAmount: Long,
    val paymentType: PaymentType,
    val cardLabel: String?,
    val purchasedTimestamp: Long,
    val note: String
)

fun PurchaseTransactionEntity.toDomain() = PurchaseTransaction(
    id = id,
    itemId = itemId,
    actualUnitPrice = actualUnitPrice,
    quantity = quantity,
    totalAmount = totalAmount,
    paymentType = paymentType,
    cardLabel = cardLabel,
    purchasedTimestamp = purchasedTimestamp,
    note = note
)

fun PurchaseTransaction.toEntity() = PurchaseTransactionEntity(
    id = id,
    itemId = itemId,
    actualUnitPrice = actualUnitPrice,
    quantity = quantity,
    totalAmount = totalAmount,
    paymentType = paymentType,
    cardLabel = cardLabel,
    purchasedTimestamp = purchasedTimestamp,
    note = note
)
