package IVS.CMS.services.dto.request;

import IVS.CMS.domain.constants.PostStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReqPostUpdateDTO {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    @NotBlank(message = "Slug (đường dẫn) không được để trống")
    private String slug;
    private String summary;
    @NotBlank(message = "Nội dung bài viết không được để trống")
    private String content;

    private String metaTitle;
    private String metaDescription;
    private String canonicalUrl;
    private Boolean isIndexable;
    private Boolean isFollowable;

    private String ogTitle;
    private String ogDescription;
    private Long ogImageId;
    private Long featuredMediaId;

    @NotNull(message = "Danh mục bài viết không được để trống")
    private Long categoryId;
    // private PostStatusEnum status;
    private LocalDateTime publishedAt;

    private List<Long> tagIds;
    private List<Long> mediaIds;
}