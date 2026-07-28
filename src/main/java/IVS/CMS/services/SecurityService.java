package IVS.CMS.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import IVS.CMS.domain.dto.response.ResLoginDTO;
import com.nimbusds.jose.util.Base64;

@Service
public class SecurityService {
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;

    @Value("${CMS.jwt.base64-secret}")
    private String jwtKey;

    @Value("${CMS.jwt.access-token-validity-in-seconds}")
    private long accessTokenJwtExpiration;

    @Value("${CMS.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public SecurityService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String createAccessToken(String email, ResLoginDTO.UserLogin user) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenJwtExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .claim("user", buildUserClaim(user))
                .build();

        return encode(claims);
    }

    public String createRefreshToken(String email, ResLoginDTO.UserLogin user) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .claim("user", buildUserClaim(user))
                .build();

        return encode(claims);
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private Map<String, Object> buildUserClaim(ResLoginDTO.UserLogin user) {
        Map<String, Object> userClaim = new HashMap<>();
        if (user == null) {
            return userClaim;
        }

        userClaim.put("id", user.getId());
        userClaim.put("email", user.getEmail());
        userClaim.put("employeeCode", user.getEmployeeCode());
        userClaim.put("fullname", user.getFullname());
        userClaim.put("avatarUrl", user.getAvatarUrl());

        if (user.getRole() != null) {
            userClaim.put("roleId", user.getRole().getId());
            userClaim.put("roleName", user.getRole().getName());
        }
        return userClaim;
    }

    public Jwt checkValidRefreshToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityService.JWT_ALGORITHM)
                .build();
        Jwt jwt = jwtDecoder.decode(token);
        if (!TOKEN_TYPE_REFRESH.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))) {
            throw new BadCredentialsException("Refresh token không hợp lệ");
        }
        return jwt;
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }

    public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
        return !hasCurrentUserAnyOfAuthorities(authorities);
    }

    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && getAuthorities(authentication)
                .anyMatch(authority -> Arrays.asList(authorities).contains(authority));
    }

    public static boolean hasCurrentUserThisAuthority(String authority) {
        return hasCurrentUserAnyOfAuthorities(authority);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority);
    }
}
