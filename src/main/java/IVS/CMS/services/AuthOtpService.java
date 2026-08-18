package IVS.CMS.services;

import IVS.CMS.domain.dto.request.ReqResetPasswordWithOtpDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.response.ResOtpVerifyDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;

public interface AuthOtpService {
    void sendRegisterOtp(ReqUserCreateDTO req);

    ResUserCreateDTO verifyRegisterOtp(String email, String otp);

    void sendForgotPasswordOtp(String email);

    ResOtpVerifyDTO verifyForgotPasswordOtp(String email, String otp);

    void resetPassword(ReqResetPasswordWithOtpDTO req);
}
