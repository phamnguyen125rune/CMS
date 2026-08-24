package IVS.CMS.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.domain.dto.request.ReqChangePasswordDTO;
import IVS.CMS.domain.dto.request.ReqUpdateProfileDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqUserUpdateDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.services.PermissionCacheService;
import IVS.CMS.services.SecurityService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ForbiddenException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.UserMapper;
import IVS.CMS.services.FileUploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_LOCKED = "LOCKED";
    private static final String DEFAULT_RESET_PASSWORD = "123456";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long[] LOCK_MINUTES = { 1, 5, 15, 30, 60 };

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final FileUploadService fileUploadService;
    private final PermissionCacheService permissionCacheService;

    @Override
    @Transactional

    public synchronized ResUserCreateDTO createUser(ReqUserCreateDTO req) {
        User user = userMapper.reqCreateToUser(req);
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setFullname(resolveStaffFullname(user.getFullname(), user.getEmail()));

        Optional<User> existingUser = this.userRepository.findByEmailIncludeDeleted(user.getEmail());
        if (existingUser.isPresent()) {
            User existing = existingUser.get();
            if (!Boolean.TRUE.equals(existing.getDeleted())) {
                throw new BadRequestException("Email " + user.getEmail() + " đã tồn tại!");
            }

            restoreDeletedStaffUser(existing, user);
            existing = this.userRepository.save(existing);
            this.permissionCacheService.evictUser(existing.getId());
            return this.userMapper.userToResCreateDTO(existing);
        }

        validateCreatableRole(user);
        applyStaffAccountDefaults(user);
        user.setEmployeeCode(generateEmployeeCode());

        user = this.userRepository.save(user);

        return this.userMapper.userToResCreateDTO(user);
    }

    @Override
    @Transactional
    public User fetchUserById(long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + id + " không tồn tại"));
    }

    @Override
    @Transactional
    public ResUserDTO getUserById(long id) {
        User user = this.fetchUserById(id);
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
    public ReqUserUpdateDTO UpdateUser(long id, ReqUserUpdateDTO req) {
        User userCurrent = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + id + " không tồn tại"));

        if (!userCurrent.getEmail().equalsIgnoreCase(req.getEmail())
                && this.userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        userCurrent.setFullname(req.getFullname());
        userCurrent.setEmail(req.getEmail());
        userCurrent.setAge(req.getAge());
        userCurrent.setGender(req.getGender());
        userCurrent.setPhone(req.getPhone());
        userCurrent.setAddress(req.getAddress());
        userCurrent.setAvatarUrl(req.getAvatarUrl());
        userCurrent.setDateOfBirth(req.getDateOfBirth());

        userCurrent = this.userRepository.save(userCurrent);
        this.permissionCacheService.evictUser(userCurrent.getId());
        return this.userMapper.userToReqUserUpdate(userCurrent);
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

        if (!this.passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        if (this.passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng mật khẩu cũ");
        }
        user.setPassword(this.passwordEncoder.encode(req.getNewPassword()));
        user.setRefreshToken(null);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        this.userRepository.save(user);
        this.userRepository.clearLoginFailures(user.getId());
        this.permissionCacheService.evictUser(user.getId());
    }

    @Override
    public User handleGetUserByEmail(String username) {
        User user = this.userRepository.findByEmail(username);

        if (user != null && user.getRole() != null && user.getRole().getId() > 0) {
            Role fullRole = this.roleRepository.findById(user.getRole().getId()).orElse(null);
            user.setRole(fullRole);
        }
        return user;
    }

    @Override
    @Transactional
    public void updateUserToken(String token, String email) {
        User currentUser = this.handleGetUserByEmail(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    @Override
    @Transactional
    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    @Override
    @Transactional
    public synchronized ResUserCreateDTO register(ReqUserCreateDTO req) {
        if (this.userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email " + req.getEmail() + " đã tồn tại!");
        }
        User user = this.userMapper.reqCreateToUser(req);
        applyDefaultRegisteredRole(user);
        user.setStatus(STATUS_ACTIVE);
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        user.setEmployeeCode(generateEmployeeCode());

        if (isBlank(req.getPassword())) {
            throw new BadRequestException("Mật khẩu đăng ký không được để trống");
        }
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        User user2 = this.userRepository.save(user);
        return this.userMapper.userToResCreateDTO(user2);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long id) {
        User targetUser = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User currentUser = getCurrentAuthenticatedUser();

        assertDeleteAllowed(
                currentUser,
                targetUser,
                "Không thể xóa tài khoản đang đăng nhập",
                "Không thể xóa tài khoản SUPER_ADMIN");

        int affectedRows = this.userRepository.softDelete(
                targetUser.getId(),
                currentUser.getId(),
                currentUser.getEmail());

        if (affectedRows != 1) {
            throw new ForbiddenException("Không thể xóa tài khoản được bảo vệ");
        }

        this.permissionCacheService.evictUser(targetUser.getId());
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long id) {
        User targetUser = this.userRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User currentUser = getCurrentAuthenticatedUser();

        assertDeleteAllowed(
                currentUser,
                targetUser,
                "Không thể xóa vĩnh viễn tài khoản đang đăng nhập",
                "Không thể xóa vĩnh viễn tài khoản SUPER_ADMIN");

        int affectedRows = this.userRepository.hardDelete(targetUser.getId(), currentUser.getId());
        if (affectedRows != 1) {
            throw new ForbiddenException("Không thể xóa vĩnh viễn tài khoản được bảo vệ");
        }

        this.permissionCacheService.evictUser(targetUser.getId());
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        User user = this.userRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        this.userRepository.restore(user.getId());
        this.permissionCacheService.evictUser(user.getId());
    }

    @Override
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }

        user.setPassword(this.passwordEncoder.encode(newPassword));
        user.setRefreshToken(null);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        this.userRepository.save(user);
        this.userRepository.resetLoginSecurity(user.getId());
        this.permissionCacheService.evictUser(user.getId());
    }

    @Override
    @Transactional
    public String recordFailedLogin(User user) {
        if (user == null) {
            return "Email hoặc mật khẩu không chính xác";
        }

        int failedAttempts = Math.max(0, user.getFailedLoginAttempts()) + 1;
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - failedAttempts;

        if (remainingAttempts <= 0) {
            int lockCount = Math.max(0, user.getLockCount()) + 1;
            long lockMinutes = resolveLockMinutes(lockCount);
            Instant lockedUntil = Instant.now().plusSeconds(lockMinutes * 60);
            this.userRepository.updateLoginSecurity(user.getId(), 0, lockCount, lockedUntil);
            this.permissionCacheService.evictUser(user.getId());
            return "Bạn đã nhập sai mật khẩu quá nhiều lần. Tài khoản bị khóa trong "
                    + lockMinutes + " phút. Hãy bấm Quên mật khẩu để lấy lại mật khẩu nếu bạn không nhớ.";
        }

        this.userRepository.updateLoginSecurity(user.getId(), failedAttempts, user.getLockCount(), null);
        if (remainingAttempts == 1) {
            return "Mật khẩu không chính xác. Bạn còn 1 lần thử trước khi tài khoản bị khóa. Hãy bấm Quên mật khẩu để lấy lại mật khẩu.";
        }

        return "Email hoặc mật khẩu không chính xác. Bạn còn " + remainingAttempts + " lần thử.";
    }

    @Override
    @Transactional
    public void clearLoginFailures(long id) {
        this.userRepository.clearLoginFailures(id);
    }

    @Override
    @Transactional
    public void resetLoginSecurity(long id) {
        this.userRepository.resetLoginSecurity(id);
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

    @Override
    public List<ResUserDTO> getDeletedUsers() {
        return this.userRepository.findDeletedUsers().stream()
                .map(userMapper::userToResUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleUserStatus(long id, String status) {
        String normalizedStatus = normalizeStatus(status);
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        if (STATUS_LOCKED.equals(normalizedStatus)) {
            assertNotSelf(user, "Không thể khóa tài khoản đang đăng nhập");
            assertNotSuperAdmin(user, "Không thể khóa tài khoản SUPER_ADMIN");
        }

        this.userRepository.updateStatus(id, normalizedStatus);
        this.permissionCacheService.evictUser(id);
    }

    @Override
    @Transactional
    public void resetUserPassword(long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        assertNotSelf(user, "Không thể reset mật khẩu tài khoản đang đăng nhập");
        assertNotSuperAdmin(user, "Không thể reset mật khẩu tài khoản SUPER_ADMIN");

        user.setPassword(this.passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        user.setRefreshToken(null);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        this.userRepository.save(user);
        this.userRepository.resetLoginSecurity(user.getId());
        this.permissionCacheService.evictUser(user.getId());
    }

    @Override
    @Transactional
    public String uploadMyAvatar(MultipartFile file) {
        String email = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Bạn chưa đăng nhập"));

        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            this.fileUploadService.deleteAvatar(user.getAvatarUrl());
        }

        String avatarUrl = this.fileUploadService.uploadAvatar(file);
        user.setAvatarUrl(avatarUrl);
        this.userRepository.save(user);

        return avatarUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public ResUserDTO getMyProfile() {
        String email = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Bạn chưa đăng nhập"));

        User user = this.userRepository.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        return this.userMapper.userToResUserDTO(user);
    }

    @Override
    @Transactional
    public ResUserDTO updateMyProfile(ReqUpdateProfileDTO req) {
        String email = SecurityService.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Bạn chưa đăng nhập"));

        User user = this.userRepository.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng hiện tại");
        }

        user.setFullname(req.getFullname());
        user.setPhone(req.getPhone());
        user.setAge(req.getAge());
        user.setAddress(req.getAddress());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());

        User updatedUser = this.userRepository.save(user);

        return this.userMapper.userToResUserDTO(updatedUser);
    }

    @Override
    public User handleGetUserByEmailOrEmployeeCode(String loginId) {
        User user = this.userRepository.findByEmailOrEmployeeCode(loginId);

        if (user != null && user.getRole() != null && user.getRole().getId() > 0) {
            Role fullRole = this.roleRepository.findById(user.getRole().getId()).orElse(null);
            user.setRole(fullRole);
        }

        return user;
    }

    private void validateCreatableRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getId() <= 0) {
            return;
        }

        Role role = this.roleRepository.findById(user.getRole().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role với id " + user.getRole().getId() + " không tồn tại"));

        if (isSuperAdminRole(role)) {
            throw new BadRequestException("Không được tạo người dùng với role SUPER_ADMIN");
        }

        user.setRole(role);
    }

    private void restoreDeletedStaffUser(User existing, User requested) {
        existing.setFullname(resolveStaffFullname(requested.getFullname(), requested.getEmail()));
        existing.setEmail(normalizeEmail(requested.getEmail()));
        existing.setAvatarUrl(requested.getAvatarUrl());
        existing.setPhone(requested.getPhone());
        existing.setAge(requested.getAge());
        existing.setAddress(requested.getAddress());
        existing.setGender(requested.getGender());
        existing.setDateOfBirth(requested.getDateOfBirth());
        existing.setRole(requested.getRole());

        validateCreatableRole(existing);
        applyStaffAccountDefaults(existing);
        existing.setDeleted(false);
        existing.setDeletedAt(null);
        existing.setDeletedBy(null);
        existing.setRefreshToken(null);
    }

    private void applyStaffAccountDefaults(User user) {
        user.setStatus(STATUS_ACTIVE);
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
    }

    private void applyDefaultRegisteredRole(User user) {
        if (user == null) {
            return;
        }

        Role normalUserRole = this.roleRepository.findByName("NORMAL_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role NORMAL_USER không tồn tại"));
        user.setRole(normalUserRole);
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

    private void assertDeleteAllowed(
            User currentUser,
            User targetUser,
            String selfDeleteMessage,
            String superAdminMessage) {
        if (currentUser.getId() == targetUser.getId()) {
            throw new ForbiddenException(selfDeleteMessage);
        }

        if (isSuperAdminUser(targetUser)) {
            throw new ForbiddenException(superAdminMessage);
        }
    }

    private boolean isSuperAdminUser(User user) {
        if (user == null || user.getRole() == null || user.getRole().getId() <= 0) {
            return false;
        }

        Role role = this.roleRepository.findById(user.getRole().getId()).orElse(user.getRole());
        return isSuperAdminRole(role);
    }

    private void assertNotSelf(User targetUser, String message) {
        String currentEmail = SecurityService.getCurrentUserLogin().orElse(null);
        if (currentEmail != null && targetUser.getEmail() != null
                && currentEmail.equalsIgnoreCase(targetUser.getEmail())) {
            throw new BadRequestException(message);
        }
    }

    private void assertNotSuperAdmin(User targetUser, String message) {
        if (targetUser.getRole() == null || targetUser.getRole().getId() <= 0) {
            return;
        }
        Role role = this.roleRepository.findById(targetUser.getRole().getId()).orElse(targetUser.getRole());
        if (isSuperAdminRole(role)) {
            throw new BadRequestException(message);
        }
    }

    private boolean isSuperAdminRole(Role role) {
        return role != null && role.getName() != null && ROLE_SUPER_ADMIN.equalsIgnoreCase(role.getName().trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String resolveStaffFullname(String fullname, String email) {
        if (!isBlank(fullname)) {
            return fullname.trim();
        }

        if (isBlank(email) || !email.contains("@")) {
            return "Nhân sự mới";
        }

        String localPart = email.substring(0, email.indexOf('@'))
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
        return localPart.isEmpty() ? "Nhân sự mới" : localPart;
    }

    private long resolveLockMinutes(int lockCount) {
        int index = Math.max(0, Math.min(lockCount - 1, LOCK_MINUTES.length - 1));
        return LOCK_MINUTES[index];
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Trạng thái không được để trống");
        }

        String normalizedStatus = status.trim().toUpperCase();
        if (!STATUS_ACTIVE.equals(normalizedStatus) && !STATUS_LOCKED.equals(normalizedStatus)) {
            throw new BadRequestException("Trạng thái không hợp lệ. Chỉ hỗ trợ ACTIVE hoặc LOCKED");
        }
        return normalizedStatus;
    }

}
