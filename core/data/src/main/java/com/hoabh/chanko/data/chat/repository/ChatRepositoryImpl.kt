package com.hoabh.chanko.data.chat.repository

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import com.hoabh.chanko.domain.chat.model.ChatGenerationEvent
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triển khai ChatRepository theo Clean Architecture.
 * Trong giai đoạn B07: Contract Ready — Pending Backend Implementation,
 * ChatRepositoryImpl ủy quyền cho FakeChatRepository chuẩn 13 endpoints.
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val delegate: FakeChatRepository
) : ChatRepository by delegate
