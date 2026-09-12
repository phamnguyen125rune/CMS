package IVS.CMS.services.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResOtpVerifyDTO {
    private String email;
    private String resetToken;
}