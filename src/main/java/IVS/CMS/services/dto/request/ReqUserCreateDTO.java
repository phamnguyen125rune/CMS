package IVS.CMS.services.dto.request;

import java.time.LocalDate;
import IVS.CMS.domain.constants.GenderEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUserCreateDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String avatarUrl;
    private String phoneNumber;
    private String address;
    private GenderEnum gender;
    private LocalDate dateOfBirth;

    @NotNull(message = "Role không được để trống")
    private Long roleId;
}