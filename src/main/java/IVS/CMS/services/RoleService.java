package IVS.CMS.services;

import java.util.List;

import IVS.CMS.domain.Role;
import IVS.CMS.services.dto.request.role.ReqRoleDTO;
import IVS.CMS.services.dto.response.role.ResListRoleDTO;

public interface RoleService {
    
    List<ResListRoleDTO> getAllRoles();

    Role createRole(ReqRoleDTO req);

    Role updateRole(Long id, ReqRoleDTO req);

    Role updateRoleByRoleName(ReqRoleDTO req);

    Role updateActiveRole(Long id);
    
    void deleteRole(Long id);
}
