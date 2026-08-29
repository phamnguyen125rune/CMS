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
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.FileUploadService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.dto.request.ReqChangePasswordDTO;
import IVS.CMS.services.dto.request.ReqUpdateProfileDTO;
import IVS.CMS.services.dto.request.ReqUserCreateDTO;
import IVS.CMS.services.dto.request.ReqUserUpdateDTO;
import IVS.CMS.services.dto.response.ResUserCreateDTO;
import IVS.CMS.services.dto.response.ResUserDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ForbiddenException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

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
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public synchronized ResUserCreateDTO createUser(ReqUserCreateDTO req) {
        User user = userMapper.reqCreateToUser(req);
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setFullName(resolveStaffFullname(user.getFullName(), user.getEmail()));

        User existing = this.userRepository.findByEmailOrEmployeeCodeIncludeDeleted(user.getEmail());
        if (existing != null) {
            if (existing.getDeletedAt() == null) {
                throw new BadRequestException("Email " + user.getEmail() + " đã tồn tại!");
            }
            restoreDeletedStaffUser(existing, user);
            existing = this.userRepository.save(existing);
            return this.userMapper.userToResCreateDTO(existing);
        }

        validateCreatableRole(user);
        applyStaffAccountDefaults(user);
        user.setEmployeeCode(generateEmployeeCode());
        user = this.userRepository.save(user);
        return this.userMapper.userToResCreateDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User fetchUserById(long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + id + " không tồn tại"));
    }

    @Override
    @Transactional(readOnly = true)
    public ResUserDTO getUserById(long id) {
        User user = this.fetchUserById(id);
        return userMapper.userToResUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
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

        userCurrent.setFullName(req.getFullName());
        userCurrent.setEmail(req.getEmail());
        userCurrent.setGender(req.getGender());
        userCurrent.setPhoneNumber(req.getPhoneNumber());
        userCurrent.setAddress(req.getAddress());
        userCurrent.setAvatarUrl(req.getAvatarUrl());
        userCurrent.setDateOfBirth(req.getDateOfBirth());
        userCurrent.setRoleId(req.getRoleId());

        userCurrent = this.userRepository.save(userCurrent);
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
        if (!this.passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }
        if (this.passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu mới không được trùng mật khẩu cũ");
        }

        Long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        String newHash = this.passwordEncoder.encode(req.getNewPassword());

        this.userRepository.updatePassword(user.getUserId(), newHash, currentUserId, LocalDateTime.now());
        this.userRepository.clearLoginFailures(user.getUserId());
    }

    @Override
    public User handleGetUserByEmail(String username) {
        return this.userRepository.findByEmail(username);
    }

    @Override
    @Transactional
    public synchronized ResUserCreateDTO register(ReqUserCreateDTO req) {
        if (this.userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email " + req.getEmail() + " đã tồn tại!");
        }
        User user = this.userMapper.reqCreateToUser(req);
        applyDefaultRegisteredRole(user);
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        user.setEmployeeCode(generateEmployeeCode());

        if (isBlank(req.getPassword())) {
            throw new BadRequestException("Mật khẩu không được để trống");
        }
        user.setPasswordHash(this.passwordEncoder.encode(req.getPassword()));

        User savedUser = this.userRepository.save(user);
        return this.userMapper.userToResCreateDTO(savedUser);
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
                targetUser.getUserId(),
                currentUser.getUserId(),
                LocalDateTime.now());

        if (affectedRows != 1) {
            throw new ForbiddenException("Không thể xóa tài khoản này");
        }
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

        int affectedRows = this.userRepository.hardDelete(targetUser.getUserId(), currentUser.getUserId());
        if (affectedRows != 1) {
            throw new ForbiddenException("Không thể xóa vĩnh viễn tài khoản này");
        }
    }

    @Override
    @Transactional
    public void restoreUser(Long id) {
        User user = this.userRepository.findByIdIncludeDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        this.userRepository.restore(user.getUserId(), currentUserId, LocalDateTime.now());
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

        long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        boolean isActive = !STATUS_LOCKED.equals(normalizedStatus);
        this.userRepository.updateStatus(id, isActive, currentUserId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void resetUserPassword(long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        assertNotSelf(user, "Không thể reset mật khẩu tài khoản đang đăng nhập");
        assertNotSuperAdmin(user, "Không thể reset mật khẩu tài khoản SUPER_ADMIN");

        long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        this.userRepository.updatePassword(user.getUserId(), this.passwordEncoder.encode(DEFAULT_RESET_PASSWORD),
                currentUserId, LocalDateTime.now());
        this.userRepository.clearLoginFailures(user.getUserId());
    }

    @Override
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }
        long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        this.userRepository.updatePassword(user.getUserId(), this.passwordEncoder.encode(newPassword), currentUserId,
                LocalDateTime.now());
        this.userRepository.clearLoginFailures(user.getUserId());
    }

    @Override
    @Transactional
    public String recordFailedLogin(User user) {
        if (user == null) {
            return "Email hoặc mật khẩu không chính xác";
        }
        int failedAttempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - failedAttempts;

        if (remainingAttempts <= 0) {
            int lockCount = (user.getLockCount() != null ? user.getLockCount() : 0) + 1;
            long lockMinutes = resolveLockMinutes(lockCount);
            LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);

            this.userRepository.updateLoginSecurity(user.getUserId(), 0, lockCount, lockedUntil);
            return "Bạn đã nhập sai mật khẩu quá nhiều lần. Tài khoản bị khóa trong "
                    + lockMinutes + " phút. Vui lòng thử lại sau hoặc Quên mật khẩu.";
        }

        this.userRepository.updateLoginSecurity(user.getUserId(), failedAttempts,
                user.getLockCount() != null ? user.getLockCount() : 0, null);
        if (remainingAttempts == 1) {
            return "Mật khẩu không chính xác. Bạn còn 1 lần thử trước khi tài khoản bị khóa.";
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
        this.userRepository.clearLoginFailures(id);
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
    @Transactional
    public String uploadUserAvatar(long id, MultipartFile file) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
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
        if (!isUsableAccount(user)) {
            throw new ForbiddenException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
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
        user.setFullName(req.getFullname());
        user.setPhoneNumber(req.getPhone());
        user.setAddress(req.getAddress());
        user.setGender(req.getGender());
        user.setDateOfBirth(req.getDateOfBirth());
        User updatedUser = this.userRepository.save(user);
        return this.userMapper.userToResUserDTO(updatedUser);
    }

    @Override
    public User handleGetUserByEmailOrEmployeeCode(String loginId) {
        return this.userRepository.findByEmailOrEmployeeCode(loginId);
    }

    @Override
    public User handleGetUserByEmailOrEmployeeCodeIncludeDeleted(String loginId) {
        return this.userRepository.findByEmailOrEmployeeCodeIncludeDeleted(loginId);
    }

    // ================= PRIVATE HELPER METHODS (BUSINESS LOGIC MANG TỪ NHÁNH CỦA
    // BẠN CẬU) ================= //

    private void validateCreatableRole(User user) {
        if (user == null || user.getRoleId() == null || user.getRoleId() <= 0) {
            return;
        }
        Role role = this.roleRepository.findById(user.getRoleId());
        if (role == null) {
            throw new ResourceNotFoundException("Role với id " + user.getRoleId() + " không tồn tại");
        }
        if (isSuperAdminRole(role)) {
            throw new BadRequestException("Không thể tạo người dùng với role SUPER_ADMIN");
        }
    }

    private void restoreDeletedStaffUser(User existing, User requested) {
        existing.setFullName(resolveStaffFullname(requested.getFullName(), requested.getEmail()));
        existing.setEmail(normalizeEmail(requested.getEmail()));
        existing.setAvatarUrl(requested.getAvatarUrl());
        existing.setPhoneNumber(requested.getPhoneNumber());
        existing.setAddress(requested.getAddress());
        existing.setGender(requested.getGender());
        existing.setDateOfBirth(requested.getDateOfBirth());
        existing.setRoleId(requested.getRoleId());

        validateCreatableRole(existing);
        applyStaffAccountDefaults(existing);

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);
    }

    private void applyStaffAccountDefaults(User user) {
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
    }

    private void applyDefaultRegisteredRole(User user) {
        if (user == null)
            return;
        Role normalUserRole = this.roleRepository.findByRoleName("NORMAL_USER");
        if (normalUserRole == null) {
            throw new ResourceNotFoundException("Role NORMAL_USER không tồn tại");
        }
        user.setRoleId(normalUserRole.getRoleId());
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

    private void assertDeleteAllowed(User currentUser, User targetUser, String selfDeleteMessage,
            String superAdminMessage) {
        if (currentUser.getUserId().equals(targetUser.getUserId())) {
            throw new ForbiddenException(selfDeleteMessage);
        }
        if (isSuperAdminUser(targetUser)) {
            throw new ForbiddenException(superAdminMessage);
        }
    }

    private boolean isSuperAdminUser(User user) {
        if (user == null || user.getRoleId() == null || user.getRoleId() <= 0) {
            return false;
        }
        Role role = this.roleRepository.findById(user.getRoleId());
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
        if (targetUser.getRoleId() == null || targetUser.getRoleId() <= 0) {
            return;
        }
        Role role = this.roleRepository.findById(targetUser.getRoleId());
        if (isSuperAdminRole(role)) {
            throw new BadRequestException(message);
        }
    }

    private boolean isSuperAdminRole(Role role) {
        return role != null && role.getRoleName() != null
                && ROLE_SUPER_ADMIN.equalsIgnoreCase(role.getRoleName().trim());
    }

    private boolean isUsableAccount(User user) {
        return user != null
                && user.getDeletedAt() == null
                && Boolean.TRUE.equals(user.getIsActive());
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
            return "Nhân viên mới";
        }
        String localPart = email.substring(0, email.indexOf('@'))
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
        return localPart.isEmpty() ? "Nhân viên mới" : localPart;
    }

    private long resolveLockMinutes(int lockCount) {
        int index = Math.max(0, Math.min(lockCount - 1, LOCK_MINUTES.length - 1));
        return LOCK_MINUTES[index];
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BadRequestException("Trạng thái không được trống");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!STATUS_ACTIVE.equals(normalizedStatus) && !STATUS_LOCKED.equals(normalizedStatus)) {
            throw new BadRequestException("Trạng thái không hợp lệ. Chỉ nhận ACTIVE hoặc LOCKED");
        }
        return normalizedStatus;
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