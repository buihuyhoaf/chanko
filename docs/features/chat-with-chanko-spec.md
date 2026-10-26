# Spec: Chat with Chanko (Android Frontend & Local Architecture)

## 1. Objective
Xây dựng module "Chat with Chanko" trên Android (Kotlin + Jetpack Compose) theo đúng chuẩn UI Candy Pop 3D, hỗ trợ 11 trạng thái màn hình (S01 - S11), tích hợp biểu cảm linh vật Chanko, hỗ trợ trò chuyện tự do (Free Chat) và tư vấn món đồ (Item Chat with Verdict), hỗ trợ đọc offline qua Room và đồng bộ theo hợp đồng Backend v1.4.

Giai đoạn đầu tập trung hoàn thiện toàn bộ **Android Frontend với Fake Fixtures & Deterministic Repositories**, gắn huy hiệu Prototype rõ ràng, kiểm thử toàn diện UX/UI trước khi cắm nối Backend FastAPI.

## 2. Tech Stack
- **Ngôn ngữ:** Kotlin
- **UI Toolkit:** Jetpack Compose + Material 3 (Theme Candy Pop 3D)
- **Kiến trúc:** MVVM + Clean Architecture (StateFlow, ViewModel, Hilt DI)
- **Lưu trữ cục bộ:** Room Database (Cache hội thoại, hỗ trợ đọc offline vô thời hạn theo hợp đồng A4)
- **Mạng (Adapter):** Retrofit + OkHttp (REST completed-response theo TECH-03, bắt đầu bằng Fake Data Source)

## 3. Commands
- Build: `./gradlew :app:assembleDebug --no-configuration-cache`
- Test: `./gradlew :app:testDebugUnitTest --no-configuration-cache`
- Lint: `./gradlew :app:lintDebug --no-configuration-cache`

## 4. Project Structure
```
app/src/main/java/com/hoabh/chanko/
├── domain/chat/
│   ├── model/ (ChatThread, ChatMessage, DecisionVerdict, MascotEmotion, Personality, ContextRevision)
│   └── repository/ (ChatRepository, ShoppingItemReadRepository, OfficialHistoryReadRepository)
├── data/chat/
│   ├── local/ (ChatDatabase, ChatDao, ChatThreadEntity, ChatMessageEntity)
│   ├── remote/ (FakeChatDataSource, ChatDto)
│   └── repository/ (ChatRepositoryImpl)
└── presentation/screens/chat/
    ├── ChatViewModel.kt
    ├── ChatUiState.kt & ChatUiEffect.kt
    ├── components/ (ChatAppBar, MessageBubble, ComposerBar, MascotHeader, VerdictCard, PrototypeBadge)
    └── screens/ (ChatScreen, ChatHistoryScreen, ItemPickerSheet, DeleteConfirmationDialog)
```

## 5. Testing Strategy
- Unit test ViewModel với StateFlow test collector (Turbine).
- Unit test Room DAO: kiểm tra ghi/đọc, pagination, stale flag 24h và bảo toàn dữ liệu offline.
- Unit test luồng FREE → ITEM bind trên cùng một thread ID.
- Unit test quy tắc độc lập của Decision Records khi xóa chat thread.

## 6. Boundaries
- **Always:** Giữ nguyên 4 Tab hiện tại (`Home`, `MyItems`, `Budget`, `Insights`); hiển thị huy hiệu Prototype khi dùng Fake Adapter; giữ nguyên tỷ lệ và màu sắc chính thức của Mascot (8 poses).
- **Ask first:** Thay đổi schema database Room nếu có nguy cơ ảnh hưởng đến entity món đồ (`ShoppingItemEntity`).
- **Never:** Thêm Tab Chat thứ 5 trên thanh điều hướng; tự động xoá dữ liệu offline quá 24h; cho phép gửi tin nhắn khi offline.
