package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Role;

@Repository
public interface RoleRepository {
    Role save(Role role);

    Optional<Role> findById(long id);

    List<Role> findAll();

    void delete(Role role);

    void updateRolePermissions(long roleId, List<Long> permissionIds);

    boolean existsByName(String name);

    Optional<Role> findByName(String name);

}