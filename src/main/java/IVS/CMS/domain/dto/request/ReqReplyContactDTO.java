package IVS.CMS.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReqReplyContactDTO {
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String replyMessage;

    public String getReplyMessage() { return replyMessage; }
    public void setReplyMessage(String replyMessage) { this.replyMessage = replyMessage; }
}
