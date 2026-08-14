package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import IVS.CMS.domain.constants.PostStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class Post {

    private Long postId;
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    @NotBlank(message = "Slug (đường dẫn) không được để trống")
    private String slug;
    private String summary;
    @NotBlank(message = "Nội dung bài viết không được để trống")
    private String content;

    private PostStatusEnum status;
    @NotNull(message = "Danh mục bài viết không được để trống")
    private Long categoryId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long createdBy;
    private Long updatedBy;
}
