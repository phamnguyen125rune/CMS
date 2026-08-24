package IVS.CMS.domain.dto.request;

import java.time.LocalDate;

import IVS.CMS.domain.constants.GenderEnum;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUserCreateDTO {
    private String fullname;
    @jakarta.validation.constraints.NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    private String password;
    private String avatarUrl;
    private String phone;
    private int age;
    private String address;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private Long roleId;
}
