package IVS.CMS.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.domain.dto.request.ReqRoleDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.services.PermissionCacheService;
import IVS.CMS.services.RoleService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final String ROLE_NORMAL_USER = "NORMAL_USER";
    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PermissionCacheService permissionCacheService;

    @Override
    @Transactional
    public Role create(ReqRoleDTO req) {
        String roleName = normalizeRoleName(req.getName());
        if (roleName.isBlank()) {
            throw new BadRequestException("Tên role không được để trống");
        }
        if (isSystemRoleName(roleName)) {
            throw new BadRequestException("Không được tạo trùng tên role hệ thống");
        }
        if (this.roleRepository.existsByName(roleName)) {
            throw new BadRequestException("Role với tên " + roleName + " đã tồn tại");
        }

        Role role = new Role();
        role.setName(roleName);
        role.setDescription(req.getDescription());
        role.setActive(req.isActive());

        Role savedRole = this.roleRepository.save(role);

        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            this.roleRepository.updateRolePermissions(savedRole.getId(), req.getPermissionIds());
            this.permissionCacheService.evictUsersByRoleId(savedRole.getId());
        }

        return this.fetchById(savedRole.getId());
    }

    @Override
    @Transactional
    public Role update(long id, ReqRoleDTO req) {
        Role currentRole = this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role với id " + id + " không tồn tại"));

        if (isSystemRole(currentRole)) {
            throw new BadRequestException("Không được chỉnh sửa role hệ thống " + currentRole.getName());
        }

        String newName = normalizeRoleName(req.getName());
        if (newName.isBlank()) {
            throw new BadRequestException("Tên role không được để trống");
        }
        if (isSystemRoleName(newName)) {
            throw new BadRequestException("Không được đổi role thường thành tên role hệ thống");
        }
        String currentName = normalizeRoleName(currentRole.getName());

        if (!currentName.equalsIgnoreCase(newName) && this.roleRepository.existsByName(newName)) {
            throw new BadRequestException("Role với tên " + req.getName() + " đã tồn tại");
        }

        currentRole.setName(newName);
        currentRole.setDescription(req.getDescription());
        currentRole.setActive(req.isActive());

        this.roleRepository.save(currentRole);

        if (req.getPermissionIds() != null) {
            this.roleRepository.updateRolePermissions(currentRole.getId(), req.getPermissionIds());
            this.permissionCacheService.evictUsersByRoleId(currentRole.getId());
        }

        return this.fetchById(currentRole.getId());
    }

    @Override
    public Role fetchById(long id) {
        return this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role với id " + id + " không tồn tại"));
    }

    @Override
    public List<Role> fetchAll() {
        return this.roleRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(long id) {
        Role role = this.fetchById(id);
        if (isSystemRole(role)) {
            throw new BadRequestException("Không được xóa role hệ thống " + role.getName());
        }

        long assignedUsers = this.userRepository.countByRoleId(role.getId());
        if (assignedUsers > 0) {
            throw new BadRequestException("Không thể xóa nhóm quyền đang có thành viên");
        }

        this.permissionCacheService.evictUsersByRoleId(role.getId());
        this.roleRepository.delete(role);
    }

    @Override
    public List<ResUserDTO> fetchUsersByRoleId(long roleId) {
        this.fetchById(roleId);

        return this.userRepository.findByRoleId(roleId).stream()
                .map(userMapper::userToResUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResUserDTO> fetchAvailableUserRoleUsers() {
        return this.userRepository.findByRoleName(ROLE_NORMAL_USER).stream()
                .map(userMapper::userToResUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResUserDTO addUserToRole(long roleId, long userId) {
        Role targetRole = this.fetchById(roleId);

        if (ROLE_NORMAL_USER.equalsIgnoreCase(targetRole.getName())) {
            throw new BadRequestException("Không thể thêm nhân viên vào chính nhóm NORMAL_USER");
        }

        if (ROLE_SUPER_ADMIN.equalsIgnoreCase(targetRole.getName())) {
            throw new BadRequestException("Không thể thêm nhân viên vào nhóm SUPER_ADMIN");
        }

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + userId + " không tồn tại"));

        if (user.getRole() == null || user.getRole().getName() == null
                || !ROLE_NORMAL_USER.equalsIgnoreCase(user.getRole().getName())) {
            throw new BadRequestException("Chỉ được thêm nhân viên đang thuộc role NORMAL_USER vào nhóm quyền khác");
        }

        user.setRole(targetRole);
        user = this.userRepository.save(user);
        this.permissionCacheService.evictUser(user.getId());

        return this.userMapper.userToResUserDTO(user);
    }

    @Override
    @Transactional
    public ResUserDTO removeUserFromRole(long roleId, long userId) {
        Role currentRole = this.fetchById(roleId);

        if (ROLE_NORMAL_USER.equalsIgnoreCase(currentRole.getName())
                || ROLE_SUPER_ADMIN.equalsIgnoreCase(currentRole.getName())) {
            throw new BadRequestException("Không thể loại người dùng khỏi nhóm hệ thống NORMAL_USER hoặc SUPER_ADMIN");
        }

        Role normalUserRole = this.roleRepository.findByName(ROLE_NORMAL_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role NORMAL_USER không tồn tại"));

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + userId + " không tồn tại"));

        if (user.getRole() == null || user.getRole().getId() != roleId) {
            throw new BadRequestException("Người dùng không thuộc nhóm đang chọn");
        }

        user.setRole(normalUserRole);
        user = this.userRepository.save(user);
        this.permissionCacheService.evictUser(user.getId());

        return this.userMapper.userToResUserDTO(user);
    }

    private boolean isSystemRole(Role role) {
        return role != null && isSystemRoleName(role.getName());
    }

    private boolean isSystemRoleName(String roleName) {
        String normalized = normalizeRoleName(roleName);
        return ROLE_NORMAL_USER.equalsIgnoreCase(normalized)
                || ROLE_SUPER_ADMIN.equalsIgnoreCase(normalized);
    }

    private String normalizeRoleName(String roleName) {
        return roleName == null ? "" : roleName.trim();
    }

}
