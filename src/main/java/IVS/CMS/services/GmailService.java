package IVS.CMS.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.BodyPart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class GmailService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.refresh.token:}")
    private String refreshToken;

    private Gmail getGmailClient() {
        String tokenToUse = (refreshToken != null && !refreshToken.isBlank())
                ? refreshToken
                : IVS.CMS.controllers.GmailAuthController.getStoredRefreshToken();

        if (tokenToUse == null || tokenToUse.isBlank()) {
            throw new RuntimeException("Chưa liên kết tài khoản Gmail. Vui lòng cấp quyền liên kết Gmail trước khi đồng bộ thư.");
        }

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(tokenToUse);

        return new Gmail.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("CMS-Mail-Manager")
                .build();
    }

    /**
     * Lấy danh sách ID email mới trong hộp thư đến (INBOX)
     */
    public List<Message> fetchInboxMessages(String query) throws Exception {
        Gmail service = getGmailClient();
        ListMessagesResponse response = service.users().messages()
                .list("me")
                .setQ(query) // ví dụ: "label:INBOX"
                .setMaxResults(10L)
                .execute();

        return response.getMessages() != null ? response.getMessages() : new ArrayList<>();
    }

    /**
     * Đọc chi tiết 1 email dạng MIME thô
     */
    public MimeMessage getMimeMessage(String messageId) throws Exception {
        Gmail service = getGmailClient();
        Message message = service.users().messages().get("me", messageId).setFormat("raw").execute();
        byte[] emailBytes = Base64.decodeBase64(message.getRaw());

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        return new MimeMessage(session, new ByteArrayInputStream(emailBytes));
    }

    /**
     * Trích xuất nội dung text từ MIME Message
     */
    public String getTextFromMimeMessage(jakarta.mail.Part p) throws Exception {
        if (p.isMimeType("text/plain")) {
            return (String) p.getContent();
        }
        if (p.isMimeType("multipart/*")) {
            MimeMultipart mp = (MimeMultipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    return (String) bp.getContent();
                }
            }
            if (mp.getCount() > 0) {
                return getTextFromMimeMessage(mp.getBodyPart(0));
            }
        }
        return "";
    }

    /**
     * Gửi email trả lời (Reply) giữ đúng Thread
     */
    public Message sendReplyEmail(String toEmail, String subject, String bodyText, String threadId, String messageIdHeader) throws Exception {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
        email.setSubject(subject.startsWith("Re:") ? subject : "Re: " + subject, "UTF-8");
        email.setText(bodyText, "UTF-8");

        if (messageIdHeader != null && !messageIdHeader.isBlank()) {
            email.setHeader("In-Reply-To", messageIdHeader);
            email.setHeader("References", messageIdHeader);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());

        Message message = new Message();
        message.setRaw(encodedEmail);
        if (threadId != null && !threadId.isBlank()) {
            message.setThreadId(threadId);
        }

        return getGmailClient().users().messages().send("me", message).execute();
    }
}