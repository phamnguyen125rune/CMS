package IVS.CMS.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Api;
import IVS.CMS.domain.Permission;
import IVS.CMS.domain.Role;
import IVS.CMS.repositories.PermissionRepository;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.services.PermissionService;
import IVS.CMS.services.dto.request.role.PermissionDTO;
import IVS.CMS.services.dto.request.role.PermissionLinkDTO;
import IVS.CMS.services.dto.request.role.ReqPermissionApiLinkDTO;
import IVS.CMS.services.dto.request.role.ReqPermissionIdDTO;
import IVS.CMS.services.dto.response.role.ResActionDTO;
import IVS.CMS.services.error.ConflictException;
import IVS.CMS.services.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    private final RoleRepository roleRepository;

    @Override
    public List<ResActionDTO> getAllActions(){
        return permissionRepository.findAllAction();
    }

    @Override
    public List<Api> getAllApis(){
        return permissionRepository.findAllApi();
    }

    @Override
    @Transactional
    public String assignPermissionToRoleById(long roleId, ReqPermissionIdDTO req) {
        Role role = roleRepository.findById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }
        if (roleRepository.checkIsSystemRole(roleId)) {
            throw new ConflictException("Can't update permission of system role");
        }
        List<Long> permissionIds = new ArrayList<>();
        for (PermissionDTO item : req.getPermissions()) {
            Permission permission = permissionRepository.findById(item.getApiId(),item.getActionId());
            if (permission == null) {
                throw new ResourceNotFoundException(
                        "Permission doesn't exist"
                );
            }
            permissionIds.add(permission.getPermissionId());
        }
        int rows = permissionRepository.updateRolePermission(role,permissionIds);

        return PermissionMessage(rows);
    }


    @Override
    @Transactional
    public String assignPermissionToRoleByApiLink(long roleId, ReqPermissionApiLinkDTO req) {
        Role role = roleRepository.findById(roleId);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }
        if (roleRepository.checkIsSystemRole(roleId)) {
            throw new ConflictException("Can't update permission of system role");
        }
        List<Long> permissionIds = new ArrayList<>();
        for (PermissionLinkDTO item : req.getPermissions()) {
            Permission permission = permissionRepository.findByLinkApi(item.getApiLink(), item.getActionName());
            if (permission == null) {
                throw new ResourceNotFoundException(
                        "Permission doesn't exist"
                );
            }
            permissionIds.add(permission.getPermissionId());
        }
        int rows = permissionRepository.updateRolePermission(role,permissionIds);

        return PermissionMessage(rows);
    }

    private String PermissionMessage(int rows){
        if (rows > 0) {
            return "Gán permission cho role thành công!!!";
        }
        return "Thất bại gán permission cho role!!!";
    }
}