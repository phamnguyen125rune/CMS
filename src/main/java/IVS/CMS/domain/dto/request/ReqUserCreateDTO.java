package IVS.CMS.domain.dto.request;

import java.time.LocalDate;

import IVS.CMS.domain.constants.GenderEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUserCreateDTO {
    @NotBlank(message = "Tên không được để trống")
    private String fullname;
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    private String password;
    private String avatarUrl;
    private String phone;
    private int age;
    private String address;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private Long roleId;
}
