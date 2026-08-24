package IVS.CMS.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCmsStatusDTO {
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
