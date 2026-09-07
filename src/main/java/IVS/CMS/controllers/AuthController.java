package IVS.CMS.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import IVS.CMS.services.AuthOtpService;
import IVS.CMS.services.AuthService;
import IVS.CMS.services.SessionEventService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.dto.request.ReqChangePasswordDTO;
import IVS.CMS.services.dto.request.ReqEmailDTO;
import IVS.CMS.services.dto.request.ReqLoginDTO;
import IVS.CMS.services.dto.request.ReqResetPasswordWithOtpDTO;
import IVS.CMS.services.dto.request.ReqVerifyOtpDTO;
import IVS.CMS.services.dto.response.ResLoginDTO;
import IVS.CMS.services.dto.response.ResOtpVerifyDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthOtpService authOtpService;
    private final SessionEventService sessionEventService;

    public AuthController(
            AuthService authService,
            UserService userService,
            AuthOtpService authOtpService,
            SessionEventService sessionEventService) {
        this.authService = authService;
        this.userService = userService;
        this.authOtpService = authOtpService;
        this.sessionEventService = sessionEventService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO, HttpServletResponse response) {
        return ResponseEntity.ok(this.authService.login(loginDTO, response));
    }

    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(this.authService.refresh(refreshToken, response));
    }

    @GetMapping(value = "/auth/session-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter sessionEvents() {
        return this.sessionEventService.subscribeCurrentUser();
    }

    @PutMapping("/auth/change-password")
    @PreAuthorize("hasAuthority('auth:EDIT')")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ReqChangePasswordDTO req) {
        this.userService.changePassword(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/forgot-password/request-otp")
    public ResponseEntity<Void> requestForgotPasswordOtp(@Valid @RequestBody ReqEmailDTO req) {
        this.authOtpService.sendForgotPasswordOtp(req.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/forgot-password/verify-otp")
    public ResponseEntity<ResOtpVerifyDTO> verifyForgotPasswordOtp(@Valid @RequestBody ReqVerifyOtpDTO req) {
        return ResponseEntity.ok(this.authOtpService.verifyForgotPasswordOtp(req.getEmail(), req.getOtp()));
    }

    @PutMapping("/auth/forgot-password/reset")
    public ResponseEntity<Void> resetForgotPassword(@Valid @RequestBody ReqResetPasswordWithOtpDTO req) {
        this.authOtpService.resetPassword(req);
        return ResponseEntity.ok().build();
    }
}