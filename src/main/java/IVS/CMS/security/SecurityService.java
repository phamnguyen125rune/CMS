package IVS.CMS.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
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

import com.nimbusds.jose.util.Base64;

import IVS.CMS.services.dto.response.ResLoginDTO;

@Service
public class SecurityService {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

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

    public String createAccessToken(ResLoginDTO.UserLogin res) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenJwtExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(res.getEmail())
                .claim("token_type", "access")
                .claim("user", res)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createRefreshToken(ResLoginDTO.UserLogin res) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(res.getEmail())
                .claim("token_type", "refresh")
                .claim("user", res)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null)
            return null;

        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static Optional<Long> getCurrentUserId() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractUserId(securityContext.getAuthentication()));
    }

    private static Long extractUserId(Authentication authentication) {
        if (authentication == null)
            return null;

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            Map<String, Object> userClaim = jwt.getClaim("user");
            if (userClaim != null && userClaim.get("id") != null) {
                Object idObj = userClaim.get("id");
                if (idObj instanceof Number number) {
                    return number.longValue();
                }
                try {
                    return Long.parseLong(String.valueOf(idObj));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public Jwt checkValidRefreshToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM)
                .build();
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (!"refresh".equals(jwt.getClaimAsString("token_type"))) {
                throw new BadCredentialsException("Token không phải Refresh Token hợp lệ");
            }
            return jwt;
        } catch (Exception e) {
            System.out.println(">>> JWT error: " + e.getMessage());
            throw new BadCredentialsException("Refresh Token không hợp lệ hoặc đã hết hạn", e);
        }
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}