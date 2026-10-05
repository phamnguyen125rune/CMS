package IVS.CMS.controllers;

import IVS.CMS.services.dto.response.RestResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gmail/oauth")
public class GmailAuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Value("${google.frontend.redirect:http://localhost:3000/vi/admin/contacts}")
    private String frontendRedirect;

    // Biến lưu tạm Refresh Token vừa nhận để GmailService dùng ngay lập tức
    private static String CURRENT_REFRESH_TOKEN = "";

    public static String getStoredRefreshToken() {
        return CURRENT_REFRESH_TOKEN;
    }

    private static final List<String> SCOPES = Collections.singletonList("https://mail.google.com/");

    /**
     * 1. Trả về link xác thực Google để Next.js mở hoặc redirect
     */
    @GetMapping({"/authorize", "/authorize-url"})
    public ResponseEntity<RestResponse<String>> authorize() {
        GoogleAuthorizationCodeRequestUrl url = new GoogleAuthorizationCodeRequestUrl(
                clientId,
                redirectUri,
                SCOPES
        )
        .setAccessType("offline")
        .set("prompt", "consent");

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Tạo URL cấp quyền Google thành công");
        response.setData(url.build());

        return ResponseEntity.ok(response);
    }

    /**
     * 2. Google redirect về đây kèm mã code xác thực -> Đổi lấy Token -> Tự chuyển hướng về Next.js CMS
     */
    @GetMapping("/callback")
    public void oauthCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        try {
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);

            GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(web);

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    SCOPES
            ).setAccessType("offline").build();

            // Đổi code lấy Access Token và Refresh Token
            GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();

            String refreshToken = tokenResponse.getRefreshToken();
            String accessToken = tokenResponse.getAccessToken();

            if (refreshToken != null && !refreshToken.isBlank()) {
                CURRENT_REFRESH_TOKEN = refreshToken;
                System.out.println("=================================================");
                System.out.println(">>> REFRESH TOKEN MỚI LẤY ĐƯỢC: " + refreshToken);
                System.out.println(">>> Hãy dán vào google.refresh.token trong application.properties nếu muốn cố định.");
                System.out.println("=================================================");
            } else {
                System.out.println(">>> Google không trả Refresh Token mới (tài khoản đã được liên kết trước đó).");
            }

            // Chuyển hướng người dùng quay lại thẳng màn hình quản lý CMS kèm trạng thái
            String separator = frontendRedirect.contains("?") ? "&" : "?";
            response.sendRedirect(frontendRedirect + separator + "gmail_linked=true");

        } catch (Exception e) {
            System.err.println("Lỗi xác thực OAuth: " + e.getMessage());
            String separator = frontendRedirect.contains("?") ? "&" : "?";
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            response.sendRedirect(frontendRedirect + separator + "error=" + java.net.URLEncoder.encode(errorMsg, java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}