package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Api;
import IVS.CMS.domain.Permission;
import IVS.CMS.domain.Role;
import IVS.CMS.services.dto.response.ResActionDTO;

@Repository
public interface PermissionRepository {
    int updateRolePermission(Role role, List<Long> permissionIds);

    Optional<Permission> findById(long id);

    List<ResActionDTO> findAllAction();

    List<Api> findAllApi();

    Permission findById(long apiId, long actionId);

    Permission findByLinkApi(String apiLink, String actionName);

    List<Permission> findByRoleId(long roleId);
}
