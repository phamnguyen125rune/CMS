package IVS.CMS.domain.dto.request;

import java.time.LocalDate;

import IVS.CMS.domain.constants.GenderEnum;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUserUpdateDTO {
    @NotBlank(message = "Tên không được để trống")
    private String fullname;
    @NotBlank(message = "Email không được để trống")
    private String email;
    private String avatarUrl;
    private String phone;
    private int age;
    private String address;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
}
