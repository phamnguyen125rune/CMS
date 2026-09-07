package IVS.CMS.services.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResRoleDTO {
    private long roleId;
    private String roleName;
    private String roleDescription;
    private Boolean isActive;
    private Boolean isSystem;
    private List<PermissionLinkDTO> permissions;
}
