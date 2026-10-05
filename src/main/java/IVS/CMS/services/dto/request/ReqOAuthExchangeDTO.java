package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReqOAuthExchangeDTO {
    @NotBlank(message = "OAuth login code không được để trống")
    private String code;
}
