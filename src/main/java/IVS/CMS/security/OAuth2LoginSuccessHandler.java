package IVS.CMS.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import IVS.CMS.services.OAuth2AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${CMS.oauth2.frontend-success-url}")
    private String frontendSuccessUrl;

    private final OAuth2AuthService oauth2AuthService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OidcUser oidcUser =
                (OidcUser) authentication.getPrincipal();

        oauth2AuthService.processGoogleUser(oidcUser);

        getRedirectStrategy()
                .sendRedirect(request, response, frontendSuccessUrl);
    }
}