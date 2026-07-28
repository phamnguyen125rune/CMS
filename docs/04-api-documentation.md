# 04. Tài Liệu Danh Sách REST APIs (API Documentation)

Tài liệu này tổng hợp toàn bộ các danh mục API endpoints được cung cấp bởi **CMS Backend**, quy chuẩn Header xác thực, thông số Swagger UI và các ví dụ cấu trúc dữ liệu gửi/nhận.

---

## 🌐 1. Swagger UI & Interactive OpenAPI

Ứng dụng hỗ trợ giao diện kiểm thử API trực quan thông qua SpringDoc OpenAPI / Swagger UI:

- **Giao diện Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Truy xuất OpenAPI Specification (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🔑 2. Authentication Header

Đối với các API yêu cầu đăng nhập, Client cần đính kèm Header:

```http
Authorization: Bearer <your_access_token>
```

---

## 📋 3. Danh Sách Các REST APIs Chi Tiết

### 3.1. Authentication APIs (`/api/v1/auth`)

| HTTP Method | API Path                       | Phân Quyền Require  | Mô Tả                                                             |
| :---------- | :----------------------------- | :------------------ | :---------------------------------------------------------------- |
| `POST`      | `/api/v1/auth/login`           | Public              | Đăng nhập tài khoản, nhận Access Token & Set Cookie Refresh Token |
| `GET`       | `/api/v1/auth/account`         | Authenticated       | Trích xuất thông tin người dùng hiện tại từ Token                 |
| `GET`       | `/api/v1/auth/refresh`         | Public (với Cookie) | Lấy Access Token mới từ Refresh Token                             |
| `POST`      | `/api/v1/auth/logout`          | Authenticated       | Đăng xuất tài khoản & Xóa Refresh Token Cookie                    |
| `PUT`       | `/api/v1/auth/change-password` | Authenticated       | Đổi mật khẩu người dùng                                           |

#### Ví dụ Body Đăng Nhập (`POST /api/v1/auth/login`):

```json
{
  "username": "admin@cms.local",
  "password": "Admin@123456"
}
```

---

### 3.2. User Management APIs (`/api/v1/users`)

| HTTP Method | API Path                | Phân Quyền Require | Mô Tả                                                       |
| :---------- | :---------------------- | :----------------- | :---------------------------------------------------------- |
| `POST`      | `/api/v1/users`         | `users:EDIT`       | Tạo mới người dùng                                          |
| `GET`       | `/api/v1/users`         | `users:VIEW`       | Lấy danh sách người dùng (Hỗ trợ Phân trang `page`, `size`) |
| `GET`       | `/api/v1/users/{id}`    | `users:VIEW`       | Lấy thông tin chi tiết người dùng theo ID                   |
| `PUT`       | `/api/v1/users/{id}`    | `users:EDIT`       | Cập nhật thông tin người dùng theo ID                       |
| `DELETE`    | `/api/v1/users/{id}`    | `users:DELETE`     | Xóa người dùng theo ID                                      |
| `PUT`       | `/api/v1/users/profile` | Authenticated      | Tự cập nhật hồ sơ cá nhân                                   |
| `POST`      | `/api/v1/users/avatar`  | Authenticated      | Upload hình ảnh đại diện (Avatar)                           |

---

### 3.3. Role Management APIs (`/api/v1/roles`)

| HTTP Method | API Path             | Phân Quyền Require | Mô Tả                                                 |
| :---------- | :------------------- | :----------------- | :---------------------------------------------------- |
| `POST`      | `/api/v1/roles`      | `roles:EDIT`       | Tạo mới Vai trò (Role)                                |
| `PUT`       | `/api/v1/roles/{id}` | `roles:EDIT`       | Cập nhật thông tin và gán danh sách Quyền cho Vai trò |
| `GET`       | `/api/v1/roles/{id}` | `roles:VIEW`       | Lấy chi tiết Vai trò kèm danh sách Permissions        |
| `GET`       | `/api/v1/roles`      | `roles:VIEW`       | Lấy danh sách tất cả các Vai trò                      |
| `DELETE`    | `/api/v1/roles/{id}` | `roles:DELETE`     | Xóa Vai trò theo ID                                   |

---

### 3.4. Permission Management APIs (`/api/v1/permissions`)

| HTTP Method | API Path                   | Phân Quyền Require   | Mô Tả                              |
| :---------- | :------------------------- | :------------------- | :--------------------------------- |
| `POST`      | `/api/v1/permissions`      | `permissions:EDIT`   | Tạo mới Quyền hạn (Permission)     |
| `PUT`       | `/api/v1/permissions/{id}` | `permissions:EDIT`   | Cập nhật Quyền hạn                 |
| `GET`       | `/api/v1/permissions/{id}` | `permissions:VIEW`   | Lấy chi tiết Quyền hạn             |
| `GET`       | `/api/v1/permissions`      | `permissions:VIEW`   | Lấy danh sách tất cả các Quyền hạn |
| `DELETE`    | `/api/v1/permissions/{id}` | `permissions:DELETE` | Xóa Quyền hạn theo ID              |

---

## 🚦 4. Bảng Mã Trạng Thái HTTP (HTTP Status Codes)

| Status Code          | Ý Nghĩa        | Trường Hợp Xảy Ra                                             |
| :------------------- | :------------- | :------------------------------------------------------------ |
| `200 OK`             | Thành công     | Thực hiện yêu cầu đọc hoặc cập nhật thành công                |
| `201 Created`        | Đã tạo mới     | Tạo mới User / Role / Permission thành công                   |
| `400 Bad Request`    | Lỗi tham số    | Dữ liệu gửi lên sai định dạng, email đã tồn tại, mật khẩu sai |
| `401 Unauthorized`   | Chưa xác thực  | Không gửi JWT Token hoặc Token hết hạn / không hợp lệ         |
| `403 Forbidden`      | Không có quyền | Người dùng không sở hữu Permission tương ứng để gọi API       |
| `404 Not Found`      | Không tìm thấy | ID người dùng / vai trò / quyền hạn không tồn tại             |
| `500 Internal Error` | Lỗi hệ thống   | Ngoại lệ hệ thống không mong muốn phía Server                 |
