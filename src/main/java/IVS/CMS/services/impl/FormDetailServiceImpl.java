package IVS.CMS.services.impl;

import IVS.CMS.domain.FormDetail;
import IVS.CMS.services.dto.request.ReqCreateFormDetailDTO;
import IVS.CMS.services.dto.request.ReqReplyFormDetailDTO;
import IVS.CMS.services.dto.response.PaginationResponseDTO;
import IVS.CMS.repositories.FormDetailRepository;
import IVS.CMS.services.FormDetailService;
import IVS.CMS.services.GmailService;
import com.google.api.services.gmail.model.Message;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FormDetailServiceImpl implements FormDetailService {

    private final FormDetailRepository formDetailRepository;
    private final GmailService gmailService;

    public FormDetailServiceImpl(FormDetailRepository formDetailRepository, GmailService gmailService) {
        this.formDetailRepository = formDetailRepository;
        this.gmailService = gmailService;
    }

    @Override
    public FormDetail createFormDetail(ReqCreateFormDetailDTO dto) {
        FormDetail formDetail = new FormDetail();
        formDetail.setFullName(dto.getFullName());
        formDetail.setEmail(dto.getEmail());
        formDetail.setPhoneNumber(dto.getPhoneNumber());
        formDetail.setCompany(dto.getCompany());
        formDetail.setFormCategoryId(dto.getFormCategoryId());
        formDetail.setMessage(dto.getMessage());
        formDetail.setStatus("new");

        return formDetailRepository.save(formDetail);
    }

    @Override
    public FormDetail getFormDetailById(Long id) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));

        // Tự động chuyển status khi nhân viên click xem chi tiết form
        if ("new".equalsIgnoreCase(formDetail.getStatus())) {
            formDetailRepository.updateStatus(id, "read");
            formDetail.setStatus("read");
        }

        return formDetail;
    }

    @Override
    public PaginationResponseDTO getAllFormDetails(String search, String status, int page, int size) {
        try {
            List<FormDetail> list = formDetailRepository.findAll(search, status, page, size);
            long totalElements = formDetailRepository.count(search, status);
            int totalPages = (int) Math.ceil((double) totalElements / size);

            PaginationResponseDTO.Meta meta = new PaginationResponseDTO.Meta();
            meta.setPage(page);
            meta.setPageSize(size);
            meta.setPages(totalPages);
            meta.setTotal(totalElements);

            PaginationResponseDTO response = new PaginationResponseDTO();
            response.setMeta(meta);
            response.setResult(list);

            return response;
        } catch (Exception e) {
            System.err.println("[FormDetailServiceImpl.getAllFormDetails] ERROR: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public FormDetail replyFormDetail(Long id, ReqReplyFormDetailDTO dto) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));

        String gmailMessageId = null;
        String gmailThreadId = formDetail.getGmailThreadId();

        try {
            // Xác định subject phản hồi
            String subject = formDetail.getFormCode() != null 
                    ? "Phản hồi yêu cầu #" + formDetail.getFormCode() 
                    : "Phản hồi yêu cầu tư vấn";

            // Gọi Gmail API gửi mail (tự giữ thread nếu đã có threadId)
            Message sentMessage = gmailService.sendReplyEmail(
                    formDetail.getEmail(),
                    subject,
                    dto.getReplyMessage(),
                    formDetail.getGmailThreadId(),
                    formDetail.getGmailMessageId()
            );

            if (sentMessage != null) {
                gmailMessageId = sentMessage.getId();
                gmailThreadId = sentMessage.getThreadId();
            }
        } catch (Exception e) {
            System.err.println("[Gmail Reply Error]: " + e.getMessage());
            throw new RuntimeException("Lỗi khi gửi email phản hồi qua Gmail API: " + e.getMessage(), e);
        }

        LocalDateTime now = LocalDateTime.now();

        // Cập nhật database với đầy đủ thông tin reply và mã thread Gmail
        formDetailRepository.updateReply(
                id, 
                dto.getReplyMessage(), 
                "replied", 
                gmailMessageId, 
                gmailThreadId, 
                now
        );

        formDetail.setReplyMessage(dto.getReplyMessage());
        formDetail.setStatus("replied");
        formDetail.setGmailMessageId(gmailMessageId);
        formDetail.setGmailThreadId(gmailThreadId);
        formDetail.setRepliedAt(now);

        return formDetail;
    }

    @Override
    public FormDetail updateFormDetailStatus(Long id, String status) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));

        formDetailRepository.updateStatus(id, status);
        formDetail.setStatus(status);

        return formDetail;
    }

    @Override
    public void deleteFormDetail(Long id) {
        FormDetail formDetail = formDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Form với ID: " + id));
        formDetailRepository.deleteById(formDetail.getFormId());
    }

    @Override
    public void syncEmailsFromGmail() {
        try {
            // Lấy danh sách email trong hộp thư đến (INBOX)
            List<Message> messages = gmailService.fetchInboxMessages("label:INBOX");

            for (Message msgSummary : messages) {
                String msgId = msgSummary.getId();
                String threadId = msgSummary.getThreadId();

                // Bỏ qua nếu email đã tồn tại trong database
                if (formDetailRepository.existsByGmailMessageId(msgId)) {
                    continue;
                }

                // Lấy chi tiết email và bóc tách nội dung
                MimeMessage mimeMessage = gmailService.getMimeMessage(msgId);
                String from = mimeMessage.getHeader("From", null);
                String subject = mimeMessage.getSubject();
                String content = gmailService.getTextFromMimeMessage(mimeMessage);

                // Tách Tên và Email từ header "From"
                String senderEmail = from;
                String senderName = from;
                if (from != null && from.contains("<") && from.contains(">")) {
                    senderName = from.substring(0, from.indexOf("<")).trim().replace("\"", "");
                    senderEmail = from.substring(from.indexOf("<") + 1, from.indexOf(">")).trim();
                }

                // Lưu vào form_details
                FormDetail form = new FormDetail();
                form.setFullName(senderName != null && !senderName.isBlank() ? senderName : "Khách hàng Gmail");
                form.setEmail(senderEmail);
                form.setPhoneNumber("N/A");
                form.setCompany("N/A");
                form.setFormCategoryId(1L);
                form.setMessage(content != null && !content.isBlank() ? content : (subject != null ? subject : "Không có nội dung"));
                form.setStatus("new");
                form.setGmailMessageId(msgId);
                form.setGmailThreadId(threadId);

                formDetailRepository.save(form);
            }
        } catch (Exception e) {
            System.err.println("[Gmail Sync Error]: " + e.getMessage());
        }
    }
}