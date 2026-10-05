package IVS.CMS.services.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.services.OAuth2AuthService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2AuthServiceImpl implements OAuth2AuthService {

    @Value("${CMS.oauth2.default-role}")
    private String defaultRoleName;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public User processGoogleUser(OidcUser oidcUser) {
        
        if (oidcUser == null || oidcUser.getSubject() == null || oidcUser.getSubject().isBlank()) {
            throw new BadRequestException("Google không trả về định danh người dùng hợp lệ");
        }

        String googleSub = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        Boolean emailVerified = oidcUser.getClaimAsBoolean("email_verified");
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(emailVerified)) {
            throw new BadRequestException("Email Google chưa được xác minh");
        }

        User byGoogleSub = userRepository.findByGoogleSub(googleSub);
        if (byGoogleSub != null) {
            if (byGoogleSub.getDeletedAt() != null || !Boolean.TRUE.equals(byGoogleSub.getIsActive())) {
                throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            return byGoogleSub;
        }

        User byEmail = userRepository.findByEmailOrEmployeeCodeIncludeDeleted(email);
        if (byEmail != null) {
            if (byEmail.getDeletedAt() != null || !Boolean.TRUE.equals(byEmail.getIsActive())) {
                throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            if (byEmail.getGoogleSub() != null && !byEmail.getGoogleSub().equals(googleSub)) {
                throw new BadRequestException("Email này đã được liên kết với một tài khoản Google khác");
            }
            userRepository.updateGoogleSub(byEmail.getUserId(), googleSub, LocalDateTime.now());
            byEmail.setGoogleSub(googleSub);
            return byEmail;
        }

        return createGoogleUser(oidcUser, googleSub, email);
    }

    private User createGoogleUser(OidcUser oidcUser, String googleSub, String email) {
        Role defaultRole = roleRepository.findByRoleName(defaultRoleName);
        if (defaultRole == null || !Boolean.TRUE.equals(defaultRole.getIsActive())) {
            throw new ResourceNotFoundException("Role mặc định cho Google login không tồn tại hoặc đã bị khóa: "
                    + defaultRoleName);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setEmail(email);
        user.setGoogleSub(googleSub);
        user.setFullName(resolveFullName(oidcUser, email));
        user.setAvatarUrl(oidcUser.getPicture());
        user.setPasswordHash(null);
        user.setRoleId(defaultRole.getRoleId());
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setLockedUntil(null);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    private String resolveFullName(OidcUser oidcUser, String email) {
        String fullName = oidcUser.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

}
