package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class FormDetail {
    private Long formId;
    private String formCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String company;
    private Long formCategoryId;
    private String message;
    private String status;
    private String replyMessage;
    private LocalDateTime createdAt;
}