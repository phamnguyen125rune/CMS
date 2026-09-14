package IVS.CMS.services.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.User;
import IVS.CMS.services.dto.request.ReqResetPasswordWithOtpDTO;
import IVS.CMS.services.dto.request.ReqUserCreateDTO;
import IVS.CMS.services.dto.response.ResOtpVerifyDTO;
import IVS.CMS.services.dto.response.ResUserCreateDTO;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.services.AuthOtpService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.error.BadRequestException;
import lombok.Data;

@Service
public class AuthOtpServiceImpl implements AuthOtpService {
    private static final Logger log = LoggerFactory.getLogger(AuthOtpServiceImpl.class);
    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final String PURPOSE_RESET_PASSWORD = "RESET_PASSWORD";
    private static final int MAX_ATTEMPTS = 5;

    private final ConcurrentMap<String, PendingOtp> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${CMS.mail.from:ivs.cms.1@gmail.com}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${CMS.otp.validity-in-minutes:5}")
    private long otpValidityMinutes;

    @Value("${CMS.otp.resend-delay-in-seconds:60}")
    private long resendDelaySeconds;

    @Value("${CMS.otp.reset-token-validity-in-minutes:10}")
    private long resetTokenValidityMinutes;

    public AuthOtpServiceImpl(
            JavaMailSender mailSender,
            UserRepository userRepository,
            UserService userService) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void sendRegisterOtp(ReqUserCreateDTO req) {
        String email = normalizeEmail(req.getEmail());
        if (this.userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email " + email + " đã tồn tại!");
        }

        ReqUserCreateDTO pendingRegistration = new ReqUserCreateDTO();
        BeanUtils.copyProperties(req, pendingRegistration);
        pendingRegistration.setEmail(email);

        PendingOtp pendingOtp = createPendingOtp(email, PURPOSE_REGISTER);
        pendingOtp.setPendingRegistration(pendingRegistration);

        if (!pendingOtp.isSent()) {
            sendOtpEmail(email, pendingOtp.getCode(), "Xác thực tài khoản CMS",
                    "Mã xác nhận đăng ký tài khoản CMS của bạn là: " + pendingOtp.getCode());
            pendingOtp.setSent(true);
            this.otpStore.put(storeKey(email, PURPOSE_REGISTER), pendingOtp);
        }
    }

    @Override
    @Transactional
    public ResUserCreateDTO verifyRegisterOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        PendingOtp pendingOtp = requireValidOtp(normalizedEmail, PURPOSE_REGISTER, otp);
        ReqUserCreateDTO pendingRegistration = pendingOtp.getPendingRegistration();
        if (pendingRegistration == null) {
            throw new BadRequestException("Không tìm thấy thông tin đăng ký");
        }

        this.otpStore.remove(storeKey(normalizedEmail, PURPOSE_REGISTER));
        return this.userService.register(pendingRegistration);
    }

    @Override
    public void sendForgotPasswordOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        User user = this.userRepository.findByEmail(normalizedEmail);
        if (user == null) {
            throw new BadRequestException("Email chưa được đăng ký");
        }

        PendingOtp pendingOtp = createPendingOtp(normalizedEmail, PURPOSE_RESET_PASSWORD);

        if (!pendingOtp.isSent()) {
            sendOtpEmail(normalizedEmail, pendingOtp.getCode(), "Khôi phục mật khẩu CMS",
                    "Mã xác minh khôi phục mật khẩu CMS của bạn là: " + pendingOtp.getCode());
            pendingOtp.setSent(true);
            this.otpStore.put(storeKey(normalizedEmail, PURPOSE_RESET_PASSWORD), pendingOtp);
        }
    }

    @Override
    public ResOtpVerifyDTO verifyForgotPasswordOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        PendingOtp pendingOtp = requireValidOtp(normalizedEmail, PURPOSE_RESET_PASSWORD, otp);
        String resetToken = UUID.randomUUID().toString();
        pendingOtp.setResetToken(resetToken);
        pendingOtp.setResetTokenExpiresAt(Instant.now().plusSeconds(resetTokenValidityMinutes * 60));
        return new ResOtpVerifyDTO(normalizedEmail, resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ReqResetPasswordWithOtpDTO req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        String normalizedEmail = normalizeEmail(req.getEmail());
        PendingOtp pendingOtp = this.otpStore.get(storeKey(normalizedEmail, PURPOSE_RESET_PASSWORD));
        if (pendingOtp == null || pendingOtp.getResetToken() == null) {
            throw new BadRequestException("Vui lòng xác minh mã OTP trước");
        }
        if (Instant.now().isAfter(pendingOtp.getResetTokenExpiresAt())) {
            this.otpStore.remove(storeKey(normalizedEmail, PURPOSE_RESET_PASSWORD));
            throw new BadRequestException("Phiên đặt lại mật khẩu đã hết hạn");
        }
        if (!pendingOtp.getResetToken().equals(req.getResetToken())) {
            throw new BadRequestException("Mã đặt lại mật khẩu không hợp lệ");
        }

        this.userService.resetPasswordByEmail(normalizedEmail, req.getNewPassword());
        this.otpStore.remove(storeKey(normalizedEmail, PURPOSE_RESET_PASSWORD));
    }

    private PendingOtp createPendingOtp(String email, String purpose) {
        String key = storeKey(email, purpose);
        PendingOtp current = this.otpStore.get(key);
        Instant now = Instant.now();
        if (current != null
                && current.getExpiresAt() != null
                && now.isBefore(current.getExpiresAt())
                && current.getNextResendAt() != null
                && now.isBefore(current.getNextResendAt())) {
            return current;
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        PendingOtp pendingOtp = new PendingOtp();
        pendingOtp.setEmail(email);
        pendingOtp.setPurpose(purpose);
        pendingOtp.setCode(code);
        pendingOtp.setExpiresAt(now.plusSeconds(otpValidityMinutes * 60));
        pendingOtp.setNextResendAt(now.plusSeconds(resendDelaySeconds));
        return pendingOtp;
    }

    private PendingOtp requireValidOtp(String email, String purpose, String otp) {
        PendingOtp pendingOtp = this.otpStore.get(storeKey(email, purpose));
        if (pendingOtp == null) {
            throw new BadRequestException("Mã OTP không tồn tại hoặc đã hết hạn");
        }
        if (Instant.now().isAfter(pendingOtp.getExpiresAt())) {
            this.otpStore.remove(storeKey(email, purpose));
            throw new BadRequestException("Mã OTP đã hết hạn");
        }
        if (pendingOtp.getAttempts() >= MAX_ATTEMPTS) {
            this.otpStore.remove(storeKey(email, purpose));
            throw new BadRequestException("Bạn đã nhập sai quá nhiều lần, vui lòng gửi lại mã");
        }
        if (!pendingOtp.getCode().equals(otp)) {
            pendingOtp.setAttempts(pendingOtp.getAttempts() + 1);
            throw new BadRequestException("Mã OTP không chính xác");
        }
        return pendingOtp;
    }

    private void sendOtpEmail(String email, String otp, String subject, String body) {
        String smtpPassword = normalizeAppPassword(mailPassword);
        if (isBlank(mailUsername) || isBlank(smtpPassword)) {
            throw new BadRequestException("Chưa cấu hình Gmail App Password cho hệ thống");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body + "\n\nMã có hiệu lực trong " + otpValidityMinutes + " phút.");

        try {
            if (this.mailSender instanceof JavaMailSenderImpl javaMailSender) {
                javaMailSender.setPassword(smtpPassword);
            }
            this.mailSender.send(message);
        } catch (MailException e) {
            log.warn("Could not send OTP email to {} using Gmail account {}: {}", email, mailUsername, e.getMessage());
            throw new BadRequestException("Không gửi được email OTP, vui lòng kiểm tra cấu hình Gmail");
        }
    }

    private String normalizeEmail(String email) {
        if (isBlank(email)) {
            throw new BadRequestException("Email không được để trống");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String storeKey(String email, String purpose) {
        return purpose + ":" + email;
    }

    private String normalizeAppPassword(String password) {
        return password == null ? null : password.replaceAll("\\s+", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Data
    private static class PendingOtp {
        private String email;
        private String purpose;
        private String code;
        private Instant expiresAt;
        private Instant nextResendAt;
        private int attempts;
        private boolean sent;
        private ReqUserCreateDTO pendingRegistration;
        private String resetToken;
        private Instant resetTokenExpiresAt;

    }
}