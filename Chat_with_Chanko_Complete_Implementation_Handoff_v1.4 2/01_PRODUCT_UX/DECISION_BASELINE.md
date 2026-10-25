# Decision baseline — approval và source recovery tách riêng

34 ID CHAT-01–20/GAP-01–08/FINAL-01–06 tiếp tục có approval do user xác nhận. **Body verification:0 VERIFIED,34 PARTIAL.** Không có original body/option gắn từng ID trong nguồn truy xuất được, nên không tự điền chủ đề hoặc rule vào ID. [Register cập nhật](decision_baseline.csv) và [danh sách từng ID cần bổ sung](../14_APPROVALS_AND_RECOVERY/MISSING_DECISION_EVIDENCE.md) ghi chính xác missing fields và hướng xử lý.

Đã khôi phục nội dung nghiệp vụ đọc được thành [RECOVERED_RULES_SYNTHESIS](RECOVERED_RULES_SYNTHESIS.md),16 nhóm rule có nguồn/target/AC. Đây là bản tổng hợp, không export nguyên văn34 quyết định. [Traceability thực thi theo REC](recovered_rule_traceability.csv) dùng được ngay; traceability34 ID cũ vẫn là evidence ledger chưa map, không chặn người triển khai đọc source rules.

Source recovery là archival task có danh sách đầu vào cụ thể, không “đợi đủ mọi nguồn rồi mới bàn giao”. Khi nguyên bản xuất hiện: giữ source/revision/hash, đối chiếu ID đúng namespace, ghi option/rule/impact/AC có căn cứ, chỉ nâng VERIFIED từng row đủ evidence. Không đổi approval hoặc yêu cầu user phê duyệt lại toàn bộ UX. GAP MyItems không thay GAP Chat.

11 core visual v1.2 nay **USER_APPROVED_OFFICIAL_VISUAL_BASELINE** theo [chỉ thị trực tiếp](../14_APPROVALS_AND_RECOVERY/USER_VISUAL_APPROVAL.md), thay v0.2. Không còn dependency tìm/export/parity v0.2. A1/A3/A5 APPROVED, A2/A4 APPROVED WITH AMENDMENT theo direct user approvalv1.4; runtime sau implementation không ảnh hưởng độ đầy đủ tài liệu.

[34-row conflict/evidence review](../14_APPROVALS_AND_RECOVERY/DECISION_CONFLICT_REVIEW_V14.md): không claim no-conflict34/34 khi thiếu original bodies. Current readable-source/business invariants đã đối chiếu, technical extensions có user approval riêng.
