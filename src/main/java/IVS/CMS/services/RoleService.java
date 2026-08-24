package IVS.CMS.services;

import java.util.List;

import IVS.CMS.domain.Role;
import IVS.CMS.services.dto.request.ReqRoleDTO;
import IVS.CMS.services.dto.response.ResUserDTO;

public interface RoleService {
    Role create(ReqRoleDTO role);

    Role update(long id, ReqRoleDTO role);

    Role fetchById(long id);

    List<Role> fetchAll();

    void delete(long id);

    List<ResUserDTO> fetchUsersByRoleId(long roleId);

    List<ResUserDTO> fetchAvailableUserRoleUsers();

    ResUserDTO addUserToRole(long roleId, long userId);

    ResUserDTO removeUserFromRole(long roleId, long userId);
}
