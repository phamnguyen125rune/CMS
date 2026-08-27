# HƯỚNG DẪN TÍCH HỢP AUDIT LOG (HỆ THỐNG LƯU VẾT THAO TÁC)

Chú thích: Có thể bấm tổ hợp phím Crtl + Shift + V để xem file.md với định dạng đẹp hơn.

Hướng dẫn gắn trigger ghi log vào các `Service` hoặc `Controller` một cách đồng bộ, tối giản nhất.

---

## 1. Cơ chế hoạt động (Tổng quan)

- Hệ thống ghi log sử dụng mô hình **Spring Application Event** chạy **Bất đồng bộ (`@Async`)**.
  - Thao tác lọc dữ liệu và ghi vào CSDL diễn ra ở luồng phụ (background thread), người dùng không phải chờ đợi.
  - Module `AuditDataSanitizer` tự động quét đệ quy để phát hiện và mã hóa các trường nhạy cảm (`password`, `token`, `secret`...) thành mã băm đối chiếu `[HASH: SHA256]`.
  - Nếu quá trình ghi log gặp lỗi, transaction nghiệp vụ chính không bị ảnh hưởng hay rollback.

---

## 2. Hướng dẫn các bước tích hợp vào Service

### Bước 1: Inject `ApplicationEventPublisher` vào Service

Sử dụng Spring Event Publisher có sẵn của Spring Boot:

```java
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    // ... các repository khác ...

    // Inject thêm Event Publisher
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
```

---

### Bước 2: Bắn Event ghi log ở cuối hàm nghiệp vụ

Sau khi dữ liệu đã được lưu vào CSDL thành công, gọi `eventPublisher.publishEvent(...)`:

```java
@Override
@Transactional
public ResPostDTO createPost(ReqPostCreateDTO req) {
    // 1. Logic nghiệp vụ & lưu CSDL như bình thường
    Post savedPost = this.postRepository.save(post);
    ResPostDTO res = this.postMapper.postToResPostDTO(savedPost);

    // 2. GẮN TRIGGER AUDIT LOG TẠI ĐÂY:
    this.eventPublisher.publishEvent(new AuditLogEvent(
            savedPost.getCreatedBy(),        // User ID thực hiện (hoặc SecurityService.getCurrentUserId().orElse(null))
            "POST",                         // Tên entity (Ví dụ: "USER", "POST", "CATEGORY", "ROLE", "AUTH")
            savedPost.getPostId(),          // ID của bản ghi bị tác động
            "CREATE",                       // Hành động (Ví dụ: "CREATE", "UPDATE", "DELETE", "STATUS", "LOGIN")
            req,                            // Request DTO (Dữ liệu gửi lên - hệ thống sẽ tự serialize JSON)
            res,                            // Response DTO (Kết quả trả về - hệ thống sẽ tự serialize JSON)
            201                             // HTTP Status Code (200, 201, 400,...)
    ));

    return res;
}
```

---

## 3. Quy ước các tham số trong `AuditLogEvent`

| Tham số            | Kiểu dữ liệu | Ý nghĩa                                                                                                                                                             | Ví dụ                                                                                        |
| :----------------- | :----------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :------------------------------------------------------------------------------------------- |
| **`userId`**       | `Long`       | ID của tài khoản thực hiện thao tác (lấy từ `SecurityService.getCurrentUserId().orElse(null)` hoặc từ token). Có thể để `null` nếu là khách vãng lai hoặc hệ thống. | `1L`, `null`                                                                                 |
| **`entityType`**   | `String`     | Tên bảng / thực thể bị tác động (viết hoa chuẩn hóa).                                                                                                               | `"USER"`, `"POST"`, `"CATEGORY"`, `"ROLE"`, `"AUTH"`, `"MEDIA"`                              |
| **`entityId`**     | `Long`       | Khóa chính (ID) của bản ghi bị tác động. Nếu hành động không gắn với 1 record cụ thể (như Login) có thể truyền `0OTHER` hoặc chính `userId`.                        | `15L3H4567890`, `0OTHER`                                                                     |
| **`action`**       | `String`     | Tên thao tác nghiệp vụ (viết hoa).                                                                                                                                  | `"CREATE"`, `"UPDATE"`, `"DELETE"`, `"CHANGE_PASSWORD"`, `"LOGIN_SUCCESS"`, `"LOGIN_FAILED"` |
| **`requestData`**  | `Object`     | Object DTO gửi lên (Request Payload). Truyền `null` nếu là thao tác xóa hoặc không có payload.                                                                      | `req`, `null`                                                                                |
| **`responseData`** | `Object`     | Object DTO kết quả trả về. Truyền `null` nếu hàm trả về `void` / xóa.                                                                                               | `res`, `null`                                                                                |
| **`statusCode`**   | `Integer`    | Mã trạng thái HTTP mong muốn ghi nhận. Mặc định là `200` nếu truyền `null`.                                                                                         | `200`, `201`, `400`                                                                          |

---

## 4. Các trường hợp mẫu thường gặp

### A. Thao tác Cập nhật (Update):

```java
this.eventPublisher.publishEvent(new AuditLogEvent(
    currentUserId, "USER", targetUserId, "UPDATE", reqUpdateDTO, resUserDTO, 200
));
```

### B. Thao tác Xóa (Delete):

```java
this.eventPublisher.publishEvent(new AuditLogEvent(
    currentUserId, "POST", postId, "DELETE", null, Map.of("message", "Deleted successfully"), 200
));
```

### C. Thao tác Đổi trạng thái (Status Change):

```java
this.eventPublisher.publishEvent(new AuditLogEvent(
    currentUserId, "USER", userId, "UPDATE_STATUS", Map.of("isActive", isActive), null, 200
));
```
