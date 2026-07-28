package IVS.CMS.services;

import java.util.List;
import IVS.CMS.domain.Permission;

public interface PermissionService {
    Permission create(Permission permission);

    Permission update(long id, Permission permission);

    Permission fetchById(long id);

    List<Permission> fetchAll();

    void delete(long id);
}