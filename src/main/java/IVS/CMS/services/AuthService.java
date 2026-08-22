package IVS.CMS.services;

import IVS.CMS.domain.dto.request.ReqLoginDTO;
import IVS.CMS.domain.dto.response.ResLoginDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    ResLoginDTO login(ReqLoginDTO loginDTO, HttpServletResponse response);

    ResLoginDTO refresh(String refreshToken, HttpServletResponse response);

    ResLoginDTO.UserGetAccount getAccount();

    void logout(HttpServletResponse response);
}