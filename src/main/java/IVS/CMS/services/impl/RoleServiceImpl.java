package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import IVS.CMS.domain.Role;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.RoleService;
import IVS.CMS.services.dto.request.role.ReqRoleDTO;
import IVS.CMS.services.dto.response.role.ResListRoleDTO;
import IVS.CMS.services.error.BadRequestException;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    final RoleRepository roleRepository;

    @Override
    public Role createRole(ReqRoleDTO req) {
        if (roleRepository.findByRoleName(req.getRoleName()) != null) {
            throw new BadRequestException("Role name already exists");
        }
        Role role = new Role();
        role.setRoleName(req.getRoleName());
        role.setRoleDescription(req.getRoleDescription());
        role.setIsActive(true);
        role.setIsSystem(req.isSystem());
        role.setCreatedAt(LocalDateTime.now());
        role.setCreatedBy(SecurityService.getCurrentUserId().orElse(null)); // TODO: Replace with actual user ID from security context
        return roleRepository.save(role);
    }

    @Override
    public List<ResListRoleDTO> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role updateRole(Long id, ReqRoleDTO req) {
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new BadRequestException("Role not found");
        }
        role.setRoleName(req.getRoleName());
        role.setRoleDescription(req.getRoleDescription());
        role.setIsActive(role.getIsActive()); // Keep the current active status
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null)); // TODO: Replace with actual user ID from security context
        return roleRepository.updateById(role);
    }

    @Override
    public Role updateRoleByRoleName(ReqRoleDTO req) {
        Role role = roleRepository.findByRoleName(req.getRoleName());
        if (role == null) {
            throw new BadRequestException("Role not found");
        }
        role.setRoleName(req.getRoleName());
        role.setRoleDescription(req.getRoleDescription());
        role.setIsActive(role.getIsActive()); // Keep the current active status
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null)); // TODO: Replace with actual user ID from security context
        return roleRepository.updateByRoleName(role);
    }

    @Override
    public Role updateActiveRole(Long id){
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new BadRequestException("Role not found");
        }
        if (roleRepository.checkIsSystemRole(id)) {
            throw new BadRequestException("Can't update status of system role");
        }
        return roleRepository.changeRoleStatus(id);
    }

    @Override
    public void deleteRole(Long id){
        Role role = roleRepository.findById(id);
        if (role == null) {
            throw new BadRequestException("Role not found");
        }

        if (roleRepository.checkIsSystemRole(id)) {
            throw new BadRequestException("Can't delete system role");
        }
        roleRepository.delete(role);
    }

}