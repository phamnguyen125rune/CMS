package IVS.CMS.services.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.RefreshToken;
import IVS.CMS.domain.User;
import IVS.CMS.repositories.RefreshTokenRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.AuthService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.dto.request.ReqLoginDTO;
import IVS.CMS.services.dto.response.ResLoginDTO;
import IVS.CMS.services.error.BadRequestException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${CMS.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    private final AuthenticationManager authenticationManager;
    private final SecurityService securityService;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public ResLoginDTO login(ReqLoginDTO loginDTO, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getLoginId(), loginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUserDB = this.userService.handleGetUserByEmailOrEmployeeCode(loginDTO.getLoginId());
        if (currentUserDB == null) {
            throw new BadRequestException("Thông tin đăng nhập không hợp lệ");
        }

        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getUserId(),
                currentUserDB.getEmail(),
                currentUserDB.getEmployeeCode(),
                currentUserDB.getFullName(),
                currentUserDB.getAvatarUrl());
        res.setUser(userLogin);

        String accessToken = this.securityService.createAccessToken(userLogin);
        String refreshTokenString = this.securityService.createRefreshToken(userLogin);
        res.setAccessToken(accessToken);

        this.refreshTokenRepository.deleteByUserId(currentUserDB.getUserId());

        RefreshToken rt = new RefreshToken();
        rt.setUserId(currentUserDB.getUserId());
        rt.setToken(refreshTokenString);
        rt.setExpiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration));
        this.refreshTokenRepository.save(rt);

        setRefreshTokenCookie(response, refreshTokenString, refreshTokenExpiration);

        return res;
    }

    @Override
    @Transactional
    public ResLoginDTO refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || "abc".equals(refreshToken)) {
            throw new BadRequestException("Bạn không có Refresh token ở cookie");
        }

        Jwt decodedToken = this.securityService.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        RefreshToken tokenInDb = this.refreshTokenRepository.findByTokenAndEmail(refreshToken, email)
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại hoặc đã bị thu hồi"));

        if (tokenInDb.getExpiredAt().isBefore(LocalDateTime.now())) {
            this.refreshTokenRepository.deleteByUserId(tokenInDb.getUserId());
            throw new BadRequestException("Refresh token đã hết hạn");
        }

        User currentUserDB = this.userService.handleGetUserByEmailOrEmployeeCode(email);
        if (currentUserDB == null || currentUserDB.getDeletedAt() != null) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị xóa");
        }

        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                currentUserDB.getUserId(),
                currentUserDB.getEmail(),
                currentUserDB.getEmployeeCode(),
                currentUserDB.getFullName(),
                currentUserDB.getAvatarUrl());
        res.setUser(userLogin);

        String accessToken = this.securityService.createAccessToken(userLogin);
        String newRefreshTokenString = this.securityService.createRefreshToken(userLogin);
        res.setAccessToken(accessToken);

        this.refreshTokenRepository.deleteByUserId(currentUserDB.getUserId());

        RefreshToken rt = new RefreshToken();
        rt.setUserId(currentUserDB.getUserId());
        rt.setToken(newRefreshTokenString);
        rt.setExpiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration));
        this.refreshTokenRepository.save(rt);

        setRefreshTokenCookie(response, newRefreshTokenString, refreshTokenExpiration);

        return res;
    }

    @Override
    public ResLoginDTO.UserGetAccount getAccount() {
        String loginId = SecurityService.getCurrentUserLogin().orElse("");
        User currentUserDB = this.userService.handleGetUserByEmailOrEmployeeCode(loginId);
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userGetAccount.setUser(new ResLoginDTO.UserLogin(
                    currentUserDB.getUserId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getEmployeeCode(),
                    currentUserDB.getFullName(),
                    currentUserDB.getAvatarUrl()));
        }
        return userGetAccount;
    }

    @Override
    @Transactional
    public void logout(HttpServletResponse response) {
        String loginId = SecurityService.getCurrentUserLogin().orElse("");
        if (loginId.isEmpty()) {
            throw new BadRequestException("Access Token không hợp lệ");
        }

        User currentUser = this.userService.handleGetUserByEmailOrEmployeeCode(loginId);
        if (currentUser != null) {
            this.refreshTokenRepository.deleteByUserId(currentUser.getUserId());
        }

        setRefreshTokenCookie(response, "", 0);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}