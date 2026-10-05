package IVS.CMS.services;

import IVS.CMS.domain.User;
import IVS.CMS.services.dto.response.ResLoginDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthTokenService {
    ResLoginDTO issueTokens(User user, HttpServletResponse response);

    void clearRefreshTokenCookie(HttpServletResponse response);
}
