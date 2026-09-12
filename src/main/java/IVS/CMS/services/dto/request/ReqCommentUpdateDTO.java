package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCommentUpdateDTO {
    @NotBlank(message = "Nội dung bình luận không được trống")
    private String commentText;

    private String imageUrl;
}