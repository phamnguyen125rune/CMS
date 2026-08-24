package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqLoginDTO {
    @NotBlank(message = "tài khoản không được để trống")
    private String loginId;
    @NotBlank(message = "password không được để trống")
    private String password;

}
