# Phê duyệt trực tiếp của người dùng — contract revision1.4

Nguồn: tin nhắn trực tiếp mới nhất của user trong chat hiện tại, ngày25/10/2026, Asia/Ho_Chi_Minh. Công cụ không cung cấp message ID hoặc thời điểm chính xác; không tạo platform message ID giả. Nội dung phê duyệt được trích nguyên văn:

> Phê duyệt Technical Contracts theo hướng:
>
> - A1-A: Approved.
> - A2-A: Approved with amendment — Free Chat phải có thể chuyển thành Item Chat trong cùng thread sau xác nhận; giữ nguyên transcript và không tự tạo item.
> - A3-A: Approved.
> - A4-A: Approved with amendment — phân biệt cache freshness TTL và offline retention; dữ liệu đã cache phải tiếp tục đọc offline theo MVP.
> - A5-A: Approved.

Hiệu lực: A1/A3/A5 được approve theo phương ánA v1.3; A2/A4 được approve với amendment trên, cụ thể hóa tại13/08. Quy tắc “Free→thread khác/context immutable sau tạo” và “cache quá24h thì chặn đọc offline” của đề xuấtv1.3 bị supersede. Các approval còn lại giữ nguyên, không yêu cầu user duyệt lại.

Đây là approved implementation addendum của Chat, không chỉnh sửa nội dung SPEC/TECH gốc. Original34 CHAT/GAP/FINAL vẫn được tôn trọng và không tự nâng VERIFIED vì thiếu nguyên văn. Optional SSE không nằm trong approval A1–A5; MVP tiếp tục baseline REST. Approval không phải bằng chứng code/API/migration đã tồn tại hoặc runtime đã pass.
