package IVS.CMS.services.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.RefreshToken;
import IVS.CMS.domain.User;
import IVS.CMS.repositories.RefreshTokenRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.AuthTokenService;
import IVS.CMS.services.dto.response.ResLoginDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    @Value("${CMS.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Value("${CMS.auth.refresh-cookie-secure}")
    private boolean refreshCookieSecure;

    private final SecurityService securityService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public ResLoginDTO issueTokens(User user, HttpServletResponse response) {
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                user.getUserId(),
                user.getEmail(),
                user.getEmployeeCode(),
                user.getFullName(),
                user.getAvatarUrl());

        String accessToken = securityService.createAccessToken(userLogin);
        String refreshToken = securityService.createRefreshToken(userLogin);

        refreshTokenRepository.deleteByUserId(user.getUserId());

        RefreshToken storedRefreshToken = new RefreshToken();
        storedRefreshToken.setUserId(user.getUserId());
        storedRefreshToken.setToken(refreshToken);
        storedRefreshToken.setExpiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration));
        refreshTokenRepository.save(storedRefreshToken);

        setRefreshTokenCookie(response, refreshToken, refreshTokenExpiration);

        ResLoginDTO result = new ResLoginDTO();
        result.setUser(userLogin);
        result.setAccessToken(accessToken);
        return result;
    }

    @Override
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        setRefreshTokenCookie(response, "", 0);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
                .secure(refreshCookieSecure)
                .httpOnly(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
