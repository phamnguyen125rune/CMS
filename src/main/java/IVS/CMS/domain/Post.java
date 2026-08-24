package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

import IVS.CMS.domain.constants.PostStatusEnum;
import jakarta.validation.constraints.NotBlank;

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
    @NotBlank(message = "Danh mục bài viết không được để trống")
    private Long categoryId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long createdBy;
    private Long updatedBy;

    // private List<Tag> tags = new ArrayList<>();

    // private List<PostMedia> postMedias = new ArrayList<>();
}
