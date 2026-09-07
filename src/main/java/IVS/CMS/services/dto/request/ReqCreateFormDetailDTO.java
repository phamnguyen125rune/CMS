package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReqCreateFormDetailDTO {
    @NotBlank(message = "Vui lòng nhập họ và tên")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập địa chỉ email")
    @Email(message = "Địa chỉ email không hợp lệ")
    private String email;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^[0-9]{9,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    private String company;

    @NotNull(message = "Vui lòng chọn danh mục form")
    private Long formCategoryId;

    @NotBlank(message = "Vui lòng nhập nội dung tin nhắn")
    @Size(min = 20, message = "Nội dung phải có ít nhất 20 ký tự")
    private String message;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Long getFormCategoryId() { return formCategoryId; }
    public void setFormCategoryId(Long formCategoryId) { this.formCategoryId = formCategoryId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}