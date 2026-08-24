package IVS.CMS.services.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRoleDTO {
    @NotBlank(message = "Tên role không được để trống")
    private String name;
    private String description;
    private boolean active = true;
    private List<Long> permissionIds;
}
