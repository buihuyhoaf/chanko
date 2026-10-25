# Evidence ledger v1.3 — recovery có giới hạn, không suy nguồn không tồn tại

| Evidence | Đã kiểm tra | Kết quả / giới hạn |
|---|---|---|
| E01 | Original handoff request trong12/original_handoff_request.txt; chat Chuẩn bị bàn giao implementation01a16e5c-99c7-77a1-b0fc-0d4a344686e6 | User xác nhận ranges34 và guards; không body/option gắn riêng ID |
| E02 | Register v0.1, v1.2 và traceability trong workspace |34 ID, thiếu body từ đầu; nhiều copies không là independent evidence |
| E03 | Chat Thiết kế tính năng chat6adcba07-ac98-83ec-809a-bdb67c01c757, read_thread10 | Công cụ hiện chỉ trả5 turns mới nhất, nextCursor=null; assistant references không cung cấp body. Không thể suy approval content từ “đồng ý” thiếu câu hỏi gốc |
| E04 | Chat Reconcile chat handoff ZIP01a16e6b-4ae3-7ea0-9108-45a13b3e721a, thử10 rồi1 turn | Tool trả history exceeds read size limit, kể cả turn1; không có nội dung được phục hồi từ turn quá lớn |
| E05 | Search workspace MD/TXT/CSV theo CHAT/FINAL IDs, register/source/recovery files | Không tìm body gắn34 ID; chỉ copies/ranges/thiếu nguồn; không trộn MyItems GAP |
| E06 | Drive search Chat/Chanko/CHAT-01/FINAL-06; broaden Decision/Recovery; metadata filter name Chat/CHAT/Chanko hoặc fullText CHAT-20 | Không có source quyết định34 phù hợp. Các kết quả unrelated không fetch hoặc dùng làm bằng chứng. Không suy toàn Drive không có file ngoài phạm vi search |
| E07 | SPEC, TECH-02, TECH-03 fetch25/10/2026; metadata trong12/evidence_v13 | Nội dung khớp snapshot normalize BOM/newlines/trim; source rules có thật nhưng không gắn34 ID |
| E08 | Chat4. Technical Design & Architecture6ad2ebe8-3dac-83ec-977a-1213bfd1dd1e | User đồng ý điều chỉnh tài liệu; không đủ body34 Chat. Không lấy approval chung để approve technical extensions mới |
| E09 | User trực tiếp trong task hiện tại |11 core PNG v1.2 được phê duyệt thành visual baseline, thay v0.2; chỉ thị tách documentation/approval/runtime |

Một source có rules đúng không đủ để nói rule đó chính là CHAT-xx cụ thể. Bản tổng hợp REC có nguồn rõ được dùng độc lập; 34 register vẫn PARTIAL. Khi nhận nguyên văn, khép đúng row, map UX/API/data/AC có bằng chứng và review divergence không sửa baseline âm thầm. Không yêu cầu phê duyệt lại toàn bộ UX.

## E10 — current user technical approval
A1/A3/A5 APPROVED, A2/A4 APPROVED WITH AMENDMENT theo USER_TECHNICAL_APPROVAL_V14.md. Đây là new direct source cho current contract semantics, không phải original body34 IDs. Prior E09 remains source of visual approval. Current conformance review separates readable-source checks from unverified legacy-ID mapping.
