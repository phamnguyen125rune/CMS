package IVS.CMS.services.dto.request.role;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReqRoleDTO {

    @NotBlank(message = "Tên role không được để trống")
    private String roleName;

    private String roleDescription;

    private boolean active = true;

    private boolean system = false;
}
