# Android Implementation Guide — approved Chat contractv1.4

Current task chỉ cập nhật tài liệu/asset; không code. Trong implementation task được giao riêng, UI/fake repositories dùng11 official visual baseline và approved contracts A1–A5. Không còn chờ phê duyệt5 nhóm. Real adapter chỉ wire khi backend revision1.4/capabilities đã triển khai và kiểm chứng; approval không tự tạo deployed API.

## Architecture/resources

Compose/M3+ViewModel/StateFlow/Coroutines/Hilt theo TECH-01 và target repo/AGENTS. Stateless UI emits events, repositories/domain coordinate; no network/business mutations in composables. Native text và supplied canonical/8 poses/Nunito/OFL/icons, no screenshot-as-UI/no replacement cat. Screen image bytes frozen; S05 confirmation behavior là approved textual A2 amendment, không ảnh tự sửa.

## Stable identity and confirmed bind

Route/entity/cache/draft existing thread keyed owner+threadID. FREE→existing item selection→explicit confirmation→BindExistingItem with key/client operationID/thread If-Match/item version. Ack/lookup updates current context chip/header/revision on **same ID**, preserves all messages and current draft. No other-thread reuse/clone/merge/item create/backfill official records during conversion. Initial direct ITEM entry may reuse existing item thread per contract; that is a separate entry flow.

Picker row tap not mutation; cancel stays FREE. Pending/UnknownOutcome reconcile before bind. After remote bind, old FREE send/retry/response cannot overwrite ITEM context or call provider under old captured revision. Read/cursor query generations include context revision, entity identity stays same. Backend replay uses original admission context before current-version rejection; no new key due lost ack.

## Repository seams

ChatRepository read/list/messages/evaluate/bind/lifecycle +operation lookup/retry; ShoppingItemReadRepository validates existing item context/capabilities; OfficialHistoryReadRepository item-owned Decision/Evaluation/Purchase reads independent conversation. Chat cache stores owner/version/context revision/fetched_at, journal minimal IDs/hash/key/deadline no raw outbox. CompanionPresenter9-domain whitelist+8-pose/state overrides, no LLM tap/BUY reward.

## Cache freshness versus retention

24h freshness only marks STALE/triggers online revalidate. Retained data remains readable offline beyond24h/48h, not no-cache due age. No-cache only true absence/actual eviction/clear/known invalidation. Keep cached_at; cursor15m and retry24h expiry never delete retained transcript. CapacityLRU200threads/10k messages/20MiB only for storage pressure; protect open transcript, no age cleanup. Session draft/rotation and item manual-draft separation unchanged.

Open/foreground/reconnect validate current context/lifecycle/capabilities/pending operation before writes. Network error preserves stale reads. Known delete/unavailable purges chat cache/draft with durable tombstone; stale response cannot resurrect. Archive readonly, bind current ITEM projection sameID. Offline no AI/bind/mutations/outbox/auto replay. Token expiry alone is not freshness retention; local identified-owner offline reads follow approved MVP security/known logout policy, not fake valid server auth.

## R07 and business boundary

A1/A3 approved: logical delete immediately denies server reads, transcript/summaries purge24h/backups30d, independent official records retained/no cascade. A4 amendment means an offline device may retain stale read until it learns deletion or actual local retention boundary. Do not promise24h offline confidentiality cutoff. Only valid post-bind evaluation creates item EvaluationRecord; conversion and chat deletion never write user Decision/Purchase/budget/cooling-off or clear Ready/restore item.

## Implementation/acceptance

Start deterministic fake fixtures with visible prototype badge; fake ack not server success. Adapt DTOs to actual approved revision/capabilities, no fake item for FREE. Inspect repo standards and implement normal migrations/tests in authorized task, not by modifying SPEC/TECH source.34 original-ID mapping remains archival PARTIAL; use REC+current user approval, no fabricated ID verification.

AC01–29, IT01–17/UI-P and AM14-01–16 cover target320dp/font200%/IME/TalkBack/decode/contrast, idempotency/race/offline retention. Runtime results are collected after implementation, not a reason documents are incomplete.
