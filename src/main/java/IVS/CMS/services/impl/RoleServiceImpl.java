package IVS.CMS.services.impl;

import java.time.LocalDateTime;
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
import IVS.CMS.services.SecurityService;
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
            throw new BadRequestException("Không thể tạo trùng tên với role hệ thống");
        }
        if (this.roleRepository.existsByName(roleName)) {
            throw new BadRequestException("Role với tên " + roleName + " đã tồn tại");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleDescription(req.getDescription());
        role.setIsActive(req.isActive());
        role.setIsSystem(false);

        role.setCreatedAt(LocalDateTime.now());
        role.setCreatedBy(SecurityService.getCurrentUserId().orElse(null));

        Role savedRole = this.roleRepository.save(role);

        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            this.roleRepository.updateRolePermissions(savedRole.getRoleId(), req.getPermissionIds());
            this.permissionCacheService.evictUsersByRoleId(savedRole.getRoleId());
        }

        return this.fetchById(savedRole.getRoleId());
    }

    @Override
    @Transactional
    public Role update(long id, ReqRoleDTO req) {
        Role currentRole = this.roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role với id " + id + " không tồn tại"));

        if (Boolean.TRUE.equals(currentRole.getIsSystem())) {
            throw new BadRequestException("Không thể chỉnh sửa thông tin role hệ thống: " + currentRole.getRoleName());
        }

        String newName = normalizeRoleName(req.getName());
        if (newName.isBlank()) {
            throw new BadRequestException("Tên role không được để trống");
        }
        if (isSystemRoleName(newName)) {
            throw new BadRequestException("Không thể đổi tên trùng với role hệ thống");
        }

        if (!currentRole.getRoleName().equalsIgnoreCase(newName) && this.roleRepository.existsByName(newName)) {
            throw new BadRequestException("Role với tên " + req.getName() + " đã tồn tại");
        }

        currentRole.setRoleName(newName);
        currentRole.setRoleDescription(req.getDescription());
        currentRole.setIsActive(req.isActive());
        currentRole.setUpdatedAt(LocalDateTime.now());
        currentRole.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

        this.roleRepository.save(currentRole);

        if (req.getPermissionIds() != null) {
            this.roleRepository.updateRolePermissions(currentRole.getRoleId(), req.getPermissionIds());
            this.permissionCacheService.evictUsersByRoleId(currentRole.getRoleId());
        }

        return this.fetchById(currentRole.getRoleId());
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

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BadRequestException("Không thể xóa role hệ thống: " + role.getRoleName());
        }

        long assignedUsers = this.userRepository.countByRoleId(role.getRoleId());
        if (assignedUsers > 0) {
            throw new BadRequestException("Không thể xóa nhóm quyền đang có thành viên");
        }

        this.permissionCacheService.evictUsersByRoleId(role.getRoleId());
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
        if (ROLE_NORMAL_USER.equalsIgnoreCase(targetRole.getRoleName())) {
            throw new BadRequestException("Không cần thêm vào nhóm NORMAL_USER");
        }
        if (ROLE_SUPER_ADMIN.equalsIgnoreCase(targetRole.getRoleName())) {
            throw new BadRequestException("Không thể thêm thủ công vào nhóm SUPER_ADMIN");
        }

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + userId + " không tồn tại"));

        if (user.getRole() == null || !ROLE_NORMAL_USER.equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new BadRequestException("Chỉ được chuyển user từ role NORMAL_USER sang nhóm mới");
        }

        user.setRole(targetRole);
        user = this.userRepository.save(user);
        this.permissionCacheService.evictUser(user.getUserId());
        return this.userMapper.userToResUserDTO(user);
    }

    @Override
    @Transactional
    public ResUserDTO removeUserFromRole(long roleId, long userId) {
        Role currentRole = this.fetchById(roleId);
        if (ROLE_NORMAL_USER.equalsIgnoreCase(currentRole.getRoleName())
                || ROLE_SUPER_ADMIN.equalsIgnoreCase(currentRole.getRoleName())) {
            throw new BadRequestException("Không thể gỡ người dùng khỏi nhóm mặc định hoặc SUPER_ADMIN");
        }

        Role normalUserRole = this.roleRepository.findByName(ROLE_NORMAL_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role NORMAL_USER không tồn tại"));

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + userId + " không tồn tại"));

        if (user.getRole() == null || user.getRole().getRoleId() != roleId) {
            throw new BadRequestException("Người dùng không thuộc nhóm đang chọn");
        }

        user.setRole(normalUserRole);
        user = this.userRepository.save(user);
        this.permissionCacheService.evictUser(user.getUserId());
        return this.userMapper.userToResUserDTO(user);
    }

    private boolean isSystemRoleName(String roleName) {
        String normalized = normalizeRoleName(roleName);
        return ROLE_NORMAL_USER.equalsIgnoreCase(normalized) || ROLE_SUPER_ADMIN.equalsIgnoreCase(normalized);
    }

    private String normalizeRoleName(String roleName) {
        return roleName == null ? "" : roleName.trim();
    }
}