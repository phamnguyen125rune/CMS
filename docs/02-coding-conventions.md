# 02. Quy Chuẩn Viết Code Java & Conventions (Coding Conventions)

Tài liệu này quy định các chuẩn mực lập trình (Coding Conventions), cách đặt tên, cấu trúc DTO, chuẩn hóa xử lý lỗi và định dạng phản hồi API đối với dự án **CMS Backend**.

---

## 🔤 1. Quy Tắc Đặt Tên (Naming Conventions)

| Đối Tượng | Quy Tắc | Ví Dụ |
| :--- | :--- | :--- |
| **Package** | Tất cả viết thường (Lowercase) | `IVS.CMS.controllers`, `IVS.CMS.services` |
| **Class / Interface / Enum** | PascalCase | `UserController`, `UserService`, `GenderEnum` |
| **Method / Variable** | camelCase | `getUserById()`, `defaultAdminEmail` |
| **Constant** | UPPER_SNAKE_CASE | `ROLE_SUPER_ADMIN`, `JWT_ALGORITHM` |
| **Database Table / Column** | snake_case | `users`, `employee_code`, `created_at` |
| **REST API Path** | kebab-case | `/api/v1/users`, `/api/v1/auth/login` |

---

## 🛠️ 2. Sử Dụng Lombok & Code Cleanliness

Dự án tích hợp thư viện **Lombok** để rút gọn boiler-plate code.

### Quy định sử dụng:
- Sử dụng `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder` trên các lớp DTO và Domain Entities.
- Tránh ghi đè thủ công `getters/setters` trừ trường hợp cần bổ sung logic tùy biến.
- Ưu tiên tiêm phụ thuộc (Dependency Injection) thông qua Constructor thay vì `@Autowired` trực tiếp trên trường:

```java
// ❌ KHÔNG KHUYÊN DÙNG (Field Injection)
@Autowired
private UserService userService;

// ✅ KHUYÊN DÙNG (Constructor Injection - Safe & Testable)
private final UserService userService;

public UserController(UserService userService) {
    this.userService = userService;
}
```

---

## 📦 3. Chuẩn Hóa Response Payload API (`RestResponse<T>`)

Tất cả các REST API endpoints trong hệ thống **bắt buộc** phải bọc dữ liệu trả về trong đối tượng chuẩn hóa `RestResponse<T>`.

### Cấu trúc JSON trả về tiêu chuẩn:

```json
{
  "statusCode": 200,
  "error": null,
  "message": "Thao tác thành công",
  "data": {
    "id": 1,
    "email": "admin@cms.local",
    "fullname": "Administrator"
  }
}
```

### Khi xảy ra lỗi (Error Response):

```json
{
  "statusCode": 400,
  "error": "Bad Request",
  "message": "Mật khẩu không chính xác hoặc tài khoản bị khóa",
  "data": null
}
```

---

## 🛡️ 4. Xử Lý Lỗi Tập Trung (Global Exception Handling)

Toàn bộ các ngoại lệ (Exceptions) trong hệ thống đều được bắt và xử lý tập trung tại lớp `@RestControllerAdvice`.

- **Ngoại lệ nghiệp vụ (`IdInvalidException`)**: Trả về `statusCode = 400 Bad Request` kèm thông báo lỗi rõ ràng.
- **Lỗi Validation (`MethodArgumentNotValidException`)**: Trả về danh sách chi tiết các trường dữ liệu vi phạm điều kiện validation.
- **Lỗi Phân quyền (`AccessDeniedException`)**: Trả về `statusCode = 403 Forbidden`.
- **Lỗi Chưa xác thực (`BadCredentialsException`)**: Trả về `statusCode = 401 Unauthorized`.

---

## 📋 5. Validation và Data Transfer Objects (DTO)

1. **Phân tách Request DTO và Response DTO**:
   - `ReqLoginDTO`, `ReqCreateUserDTO`, `ReqUpdateUserDTO` xử lý dữ liệu đầu vào.
   - `ResLoginDTO`, `ResUserDTO` xử lý dữ liệu trả về cho client.
2. **Khai báo Bean Validation**:
   - Sử dụng `@NotBlank`, `@Email`, `@Size`, `@NotNull` trên các field DTO đầu vào.
   - Bắt buộc gắn `@Valid` tại điểm tiếp nhận trong Controller:

```java
@PostMapping("/users")
public ResponseEntity<RestResponse<ResUserDTO>> createUser(@Valid @RequestBody ReqCreateUserDTO userDto) {
    // Logic xử lý...
}
```

---

## 📝 6. Logging Standard

- Tránh tuyệt đối việc sử dụng `System.out.println()`.
- Sử dụng Logger từ SLF4J (hoặc Lombok `@Slf4j`):

```java
log.info("Khởi tạo mặc định thành công người dùng Admin với email: {}", adminEmail);
log.error("Lỗi khi kết nối cơ sở dữ liệu: ", ex);
```
