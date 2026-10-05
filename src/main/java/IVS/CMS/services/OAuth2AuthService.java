package IVS.CMS.services;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import IVS.CMS.domain.User;

public interface OAuth2AuthService {
    User processGoogleUser(OidcUser oidcUser);
}