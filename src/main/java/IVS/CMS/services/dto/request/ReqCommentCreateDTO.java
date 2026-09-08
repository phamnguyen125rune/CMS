package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCommentCreateDTO {
    @NotNull(message = "ID bài viết không được trống")
    private Long postId;

    private Long parentId;

    @NotBlank(message = "Nội dung bình luận không được trống")
    private String commentText;

    private String imageUrl;
}