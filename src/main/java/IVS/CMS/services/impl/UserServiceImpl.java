package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.domain.dto.request.ReqChangePasswordDTO;
import IVS.CMS.domain.dto.request.ReqUpdateProfileDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqUserUpdateDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.RefreshTokenRepository;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.services.FileUploadService;
import IVS.CMS.services.PermissionCacheService;
import IVS.CMS.services.SecurityService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ForbiddenException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_RESET_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileUploadService fileUploadService;
    private final PermissionCacheService permissionCacheService;

    @Override
    @Transactional
    public synchronized ResUserCreateDTO createUser(ReqUserCreateDTO req) {
        if (this.userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email '" + req.getEmail() + "' đã tồn tại!");
        }
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && this.userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại '" + req.getPhoneNumber() + "' đã tồn tại!");
        }

        Role role = this.roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role id " + req.getRoleId() + " không tồn tại"));

        if (Boolean.TRUE.equals(role.getIsSystem()) && "SUPER_ADMIN".equalsIgnoreCase(role.getRoleName())) {
            throw new BadRequestException("Không thể tạo người dùng gắn với role SUPER_ADMIN");
        }

        User user = userMapper.reqCreateToUser(req);
        user.setRole(role);
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setEmployeeCode(generateEmployeeCode());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(SecurityService.getCurrentUserId().orElse(null));

        User savedUser = this.userRepository.save(user);
        return this.userMapper.userToResCreateDTO(savedUser);
    }

    @Override
    public User fetchUserById(long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + userId + " không tồn tại"));
    }

    @Override
    public ResUserDTO getUserById(long userId) {
        User user = this.fetchUserById(userId);
        return userMapper.userToResUserDTO(user);
    }

    @Override
    public ResultPaginationDTO findAll(int page, int pageSize) {
        if (page < 1)
            page = 1;
        if (pageSize < 1)
            pageSize = 10;

        long total = this.userRepository.count();
        int pages = (int) Math.ceil((double) total / pageSize);
        int offset = (page - 1) * pageSize;

        List<User> users = this.userRepository.findAll(pageSize, offset);
        List<ResUserDTO> listUserRes = users.stream()
                .map(userMapper::userToResUserDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(pageSize);
        meta.setPages(pages);
        meta.setTotal(total);

        ResultPaginationDTO res = new ResultPaginationDTO();
        res.setMeta(meta);
        res.setResult(listUserRes);
        return res;
    }

    @Override
    @Transactional
    public ResUserDTO updateUser(long userId, ReqUserUpdateDTO req) {
        User userCurrent = this.fetchUserById(userId);

        if (!userCurrent.getEmail().equalsIgnoreCase(req.getEmail())
                && this.userRepository.existsByEmailForUpdate(userId, req.getEmail())) {
            throw new BadRequestException("Email '" + req.getEmail() + "' đã được sử dụng");
        }
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && this.userRepository.existsByPhoneNumberForUpdate(userId, req.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại '" + req.getPhoneNumber() + "' đã được sử dụng");
        }

        if (req.getRoleId() != null) {
            Role role = this.roleRepository.findById(req.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role id " + req.getRoleId() + " không tồn tại"));
            userCurrent.setRole(role);
            userCurrent.setRoleId(role.getRoleId());
        }

        userCurrent.setFullName(req.getFullName());
        userCurrent.setEmail(req.getEmail());
        userCurrent.setPhoneNumber(req.getPhoneNumber());
        userCurrent.setAddress(req.getAddress());
        userCurrent.setAvatarUrl(req.getAvatarUrl());
        userCurrent.setGender(req.getGender());
        userCurrent.setDateOfBirth(req.getDateOfBirth());
        if (req.getIsActive() != null) {
            userCurrent.setIsActive(req.getIsActive());
        }

        userCurrent.setUpdatedAt(LocalDateTime.now());
        userCurrent.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

        User updatedUser = this.userRepository.save(userCurrent);
        this.permissionCacheService.evictUser(updatedUser.getUserId());
        return this.userMapper.userToResUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(ReqChangePasswordDTO req) {
        String email = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Bạn chưa đăng nhập"));

        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        if (!this.passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        if (this.passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        LocalDateTime now = LocalDateTime.now();
        this.userRepository.updatePassword(user.getUserId(), this.passwordEncoder.encode(req.getNewPassword()),
                user.getUserId(), now);
        this.refreshTokenRepository.deleteByUserId(user.getUserId());
        this.permissionCacheService.evictUser(user.getUserId());
    }

    @Override
    public User handleGetUserByEmail(String email) {
        User user = this.userRepository.findByEmail(email);
        if (user != null && user.getRoleId() != null && user.getRoleId() > 0) {
            this.roleRepository.findById(user.getRoleId()).ifPresent(user::setRole);
        }
        return user;
    }

    @Override
    public User handleGetUserByEmailOrEmployeeCode(String loginId) {
        User user = this.userRepository.findByEmailOrEmployeeCode(loginId);
        if (user != null && user.getRoleId() != null && user.getRoleId() > 0) {
            this.roleRepository.findById(user.getRoleId()).ifPresent(user::setRole);
        }
        return user;
    }

    @Override
    @Transactional
    public synchronized ResUserCreateDTO register(ReqUserCreateDTO req) {
        return createUser(req);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long userId) {
        User targetUser = this.fetchUserById(userId);
        User currentUser = getCurrentAuthenticatedUser();

        assertDeleteAllowed(currentUser, targetUser, "Không thể xóa tài khoản đang đăng nhập",
                "Không thể xóa tài khoản hệ thống");

        LocalDateTime now = LocalDateTime.now();
        int affectedRows = this.userRepository.softDelete(targetUser.getUserId(), currentUser.getUserId(), now);
        if (affectedRows != 1) {
            throw new ForbiddenException("Không thể xóa tài khoản này");
        }
        this.refreshTokenRepository.deleteByUserId(targetUser.getUserId());
        this.permissionCacheService.evictUser(targetUser.getUserId());
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long userId) {
        User targetUser = this.userRepository.findByIdIncludeDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User currentUser = getCurrentAuthenticatedUser();

        assertDeleteAllowed(currentUser, targetUser, "Không thể xóa tài khoản đang đăng nhập",
                "Không thể xóa tài khoản hệ thống");

        if (targetUser.getDeletedAt() == null) {
            throw new BadRequestException("Chỉ xóa vĩnh viễn người dùng đã ở trong thùng rác");
        }

        int affectedRows = this.userRepository.hardDelete(targetUser.getUserId(), currentUser.getUserId());
        if (affectedRows != 1) {
            throw new ForbiddenException("Không thể xóa tài khoản này");
        }
        this.refreshTokenRepository.deleteByUserId(targetUser.getUserId());
        this.permissionCacheService.evictUser(targetUser.getUserId());
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        User user = this.userRepository.findByIdIncludeDeleted(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User currentUser = getCurrentAuthenticatedUser();

        this.userRepository.restore(user.getUserId(), currentUser.getUserId(), LocalDateTime.now());
        this.permissionCacheService.evictUser(user.getUserId());
    }

    @Override
    public List<ResUserDTO> getDeletedUsers() {
        return this.userRepository.findDeletedUsers().stream()
                .map(userMapper::userToResUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleUserStatus(long userId, Boolean isActive) {
        if (isActive == null) {
            throw new BadRequestException("Trạng thái isActive không hợp lệ");
        }

        User user = this.fetchUserById(userId);
        User currentUser = getCurrentAuthenticatedUser();

        if (Boolean.FALSE.equals(isActive)) {
            if (currentUser.getUserId().equals(user.getUserId())) {
                throw new BadRequestException("Không thể tự khóa tài khoản đang đăng nhập");
            }
            if (Boolean.TRUE.equals(user.getIsSystem())) {
                throw new BadRequestException("Không thể khóa tài khoản hệ thống");
            }
            this.refreshTokenRepository.deleteByUserId(user.getUserId());
        }

        this.userRepository.updateStatus(userId, isActive, currentUser.getUserId(), LocalDateTime.now());
        this.permissionCacheService.evictUser(userId);
    }

    @Override
    @Transactional
    public void resetUserPassword(long userId) {
        User user = this.fetchUserById(userId);
        User currentUser = getCurrentAuthenticatedUser();

        if (currentUser.getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Không thể reset mật khẩu tài khoản đang đăng nhập");
        }
        if (Boolean.TRUE.equals(user.getIsSystem())) {
            throw new BadRequestException("Không thể reset mật khẩu tài khoản hệ thống");
        }

        this.userRepository.updatePassword(userId, this.passwordEncoder.encode(DEFAULT_RESET_PASSWORD),
                currentUser.getUserId(), LocalDateTime.now());
        this.refreshTokenRepository.deleteByUserId(user.getUserId());
        this.permissionCacheService.evictUser(user.getUserId());
    }

    @Override
    @Transactional
    public String uploadMyAvatar(MultipartFile file) {
        User user = getCurrentAuthenticatedUser();

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            this.fileUploadService.deleteAvatar(user.getAvatarUrl());
        }

        String avatarUrl = this.fileUploadService.uploadAvatar(file);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(user.getUserId());

        this.userRepository.save(user);
        return avatarUrl;
    }

    @Override
    public ResUserDTO getMyProfile() {
        User user = getCurrentAuthenticatedUser();
        return this.userMapper.userToResUserDTO(user);
    }

    @Override
    @Transactional
    public ResUserDTO updateMyProfile(ReqUpdateProfileDTO req) {
        User user = getCurrentAuthenticatedUser();

        user.setFullName(req.getFullname());
        user.setPhoneNumber(req.getPhone());
        user.setAddress(req.getAddress());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(user.getUserId());

        User updatedUser = this.userRepository.save(user);
        return this.userMapper.userToResUserDTO(updatedUser);
    }

    private User getCurrentAuthenticatedUser() {
        String currentEmail = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new ForbiddenException("Không xác định được tài khoản đang đăng nhập"));
        User currentUser = this.userRepository.findByEmail(currentEmail);
        if (currentUser == null) {
            throw new ForbiddenException("Không tìm thấy tài khoản đang đăng nhập");
        }
        return currentUser;
    }

    private void assertDeleteAllowed(User currentUser, User targetUser, String selfDeleteMsg, String systemMsg) {
        if (currentUser.getUserId().equals(targetUser.getUserId())) {
            throw new ForbiddenException(selfDeleteMsg);
        }
        if (Boolean.TRUE.equals(targetUser.getIsSystem())) {
            throw new ForbiddenException(systemMsg);
        }
    }

    private String generateEmployeeCode() {
        String maxCode = this.userRepository.findMaxEmployeeCode();
        if (maxCode == null || maxCode.trim().isEmpty()) {
            return "EMP0001";
        }
        try {
            int currentNum = Integer.parseInt(maxCode.substring(3));
            return String.format("EMP%04d", currentNum + 1);
        } catch (Exception e) {
            return "EMP0001";
        }
    }
}