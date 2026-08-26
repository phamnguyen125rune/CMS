package IVS.CMS.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Role;
import IVS.CMS.services.dto.response.ResRoleDTO;

@Repository
public interface RoleRepository {

    List<ResRoleDTO> findAll();

    Role save(Role role);

    Role updateById(Role role);

    Role updateByRoleName(Role role);

    Role findById(Long id);

    Role changeRoleStatus(Role role);

    Role findByRoleName(String roleName);

    Boolean checkIsSystemRole(Long id);

    void delete(Role role);
}