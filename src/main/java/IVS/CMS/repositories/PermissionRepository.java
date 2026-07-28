package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Permission;

@Repository
public interface PermissionRepository {
    Permission save(Permission permission);

    Optional<Permission> findById(long id);

    List<Permission> findAll();

    void delete(Permission permission);

    List<Permission> findByRoleId(long roleId);
}
