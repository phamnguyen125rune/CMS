package IVS.CMS.services.dto.request;

import java.time.LocalDate;

import IVS.CMS.domain.constants.GenderEnum;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateProfileDTO {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullname;

    private String phone;

    private int age;

    private String address;

    private GenderEnum gender;

    private LocalDate dateOfBirth;
}