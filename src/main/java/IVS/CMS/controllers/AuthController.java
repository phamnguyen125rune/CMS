package IVS.CMS.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.User;
import IVS.CMS.domain.dto.request.ReqChangePasswordDTO;
import IVS.CMS.domain.dto.request.ReqEmailDTO;
import IVS.CMS.domain.dto.request.ReqLoginDTO;
import IVS.CMS.domain.dto.request.ReqResetPasswordWithOtpDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqVerifyOtpDTO;
import IVS.CMS.domain.dto.response.ResLoginDTO;
import IVS.CMS.domain.dto.response.ResLoginDTO.RoleLogin;
import IVS.CMS.domain.dto.response.ResLoginDTO.UserLogin;
import IVS.CMS.domain.dto.response.ResOtpVerifyDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.services.AuthOtpService;
import IVS.CMS.services.PermissionCacheService;
import IVS.CMS.services.SecurityService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.error.BadRequestException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1")
public class AuthController {
    @Value("${CMS.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityService securityService;
    private final UserService userService;
    private final PermissionCacheService permissionCacheService;
    private final AuthOtpService authOtpService;

    public AuthController(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityService securityService,
            UserService userService,
            PermissionCacheService permissionCacheService,
            AuthOtpService authOtpService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityService = securityService;
        this.userService = userService;
        this.permissionCacheService = permissionCacheService;
        this.authOtpService = authOtpService;
    }

    private UserLogin buildUserLoginDTO(User currentUserDB) {
        RoleLogin roleLogin = null;
        if (currentUserDB.getRole() != null) {
            List<String> permissions = new ArrayList<>(this.permissionCacheService.getPermissionCodes(currentUserDB));
            roleLogin = new RoleLogin(
                    currentUserDB.getRole().getId(),
                    currentUserDB.getRole().getName(),
                    permissions);
        }

        return new UserLogin(
                currentUserDB.getId(),
                currentUserDB.getEmail(),
                currentUserDB.getEmployeeCode(),
                currentUserDB.getFullname(),
                currentUserDB.getAvatarUrl(),
                roleLogin);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getLoginId(),
                loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUserDB = this.userService.handleGetUserByEmailOrEmployeeCode(loginDTO.getLoginId());
        if (currentUserDB == null) {
            throw new BadRequestException("Thông tin đăng nhập không hợp lệ");
        }

        this.permissionCacheService.cacheUser(currentUserDB);

        ResLoginDTO res = new ResLoginDTO();
        res.setUser(buildUserLoginDTO(currentUserDB));

        String email = currentUserDB.getEmail();
        String accessToken = this.securityService.createAccessToken(email, res.getUser());
        res.setAccessToken(accessToken);

        String refreshToken = this.securityService.createRefreshToken(email, res.getUser());
        this.userService.updateUserToken(refreshToken, email);

        ResponseCookie resCookie = ResponseCookie.from("refresh_token", refreshToken)
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(res);
    }

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken) {

        if (refreshToken.equals("abc")) {
            throw new BadRequestException("Bạn không có Refresh token ở cookie");
        }

        Jwt decodedToken = this.securityService.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (currentUser == null) {
            throw new BadRequestException("Refresh token không hợp lệ");
        }

        User currentUserDB = this.userService.handleGetUserByEmail(email);
        if (currentUserDB == null) {
            throw new BadRequestException("Người dùng không tồn tại");
        }
        if (isLockedUser(currentUserDB)) {
            this.permissionCacheService.evictUser(currentUserDB.getId());
            throw new BadRequestException("Tài khoản đã bị khóa, không thể cấp lại token");
        }

        this.permissionCacheService.cacheUser(currentUserDB);

        ResLoginDTO res = new ResLoginDTO();
        res.setUser(buildUserLoginDTO(currentUserDB));

        String accessToken = this.securityService.createAccessToken(email, res.getUser());
        res.setAccessToken(accessToken);

        String newRefreshToken = this.securityService.createRefreshToken(email, res.getUser());
        this.userService.updateUserToken(newRefreshToken, email);

        ResponseCookie resCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(res);
    }

    private boolean isLockedUser(User user) {
        return user != null && "LOCKED".equalsIgnoreCase(user.getStatus());
    }

    @PutMapping("/auth/change-password")
    @PreAuthorize("hasAuthority('auth:EDIT')")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ReqChangePasswordDTO req) {
        this.userService.changePassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/register/request-otp")
    public ResponseEntity<Void> requestRegisterOtp(@Valid @RequestBody ReqUserCreateDTO req) {
        this.authOtpService.sendRegisterOtp(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/register/verify-otp")
    public ResponseEntity<ResUserCreateDTO> verifyRegisterOtp(@Valid @RequestBody ReqVerifyOtpDTO req) {
        ResUserCreateDTO res = this.authOtpService.verifyRegisterOtp(req.getEmail(), req.getOtp());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/auth/forgot-password/request-otp")
    public ResponseEntity<Void> requestForgotPasswordOtp(@Valid @RequestBody ReqEmailDTO req) {
        this.authOtpService.sendForgotPasswordOtp(req.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/forgot-password/verify-otp")
    public ResponseEntity<ResOtpVerifyDTO> verifyForgotPasswordOtp(@Valid @RequestBody ReqVerifyOtpDTO req) {
        ResOtpVerifyDTO res = this.authOtpService.verifyForgotPasswordOtp(req.getEmail(), req.getOtp());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/auth/forgot-password/reset")
    public ResponseEntity<Void> resetForgotPassword(@Valid @RequestBody ReqResetPasswordWithOtpDTO req) {
        this.authOtpService.resetPassword(req);
        return ResponseEntity.ok().build();
    }
}
