// package IVS.CMS.services.impl;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.stereotype.Service;

// import IVS.CMS.domain.Permission;
// import IVS.CMS.repositories.PermissionRepository;
// import IVS.CMS.services.PermissionCacheService;
// import IVS.CMS.services.PermissionService;
// import IVS.CMS.services.SecurityService;
// import IVS.CMS.services.error.ResourceNotFoundException;

// @Service
// public class PermissionServiceImpl implements PermissionService {

//     private final PermissionRepository permissionRepository;
//     private final PermissionCacheService permissionCacheService;

//     public PermissionServiceImpl(PermissionRepository permissionRepository,
//             PermissionCacheService permissionCacheService) {
//         this.permissionRepository = permissionRepository;
//         this.permissionCacheService = permissionCacheService;
//     }

//     @Override
//     public Permission create(Permission permission) {
//         permission.setCreatedAt(LocalDateTime.now());
//         permission.setCreatedBy(SecurityService.getCurrentUserId().orElse(null));

//         Permission savedPermission = this.permissionRepository.save(permission);
//         this.permissionCacheService.evictAll();
//         return savedPermission;
//     }

//     @Override
//     public Permission update(long id, Permission permission) {
//         Permission currentPermission = this.fetchById(id);

//         currentPermission.setActionId(permission.getActionId());
//         currentPermission.setApiId(permission.getApiId());

//         currentPermission.setUpdatedAt(LocalDateTime.now());
//         currentPermission.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

//         Permission savedPermission = this.permissionRepository.save(currentPermission);
//         this.permissionCacheService.evictAll();
//         return savedPermission;
//     }

//     @Override
//     public Permission fetchById(long id) {
//         return this.permissionRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("Permission với id " + id + " không tồn tại"));
//     }

//     @Override
//     public List<Permission> fetchAll() {
//         return this.permissionRepository.findAll();
//     }

//     @Override
//     public void delete(long id) {
//         Permission currentPermission = this.fetchById(id);
//         this.permissionRepository.delete(currentPermission);
//         this.permissionCacheService.evictAll();
//     }
// }