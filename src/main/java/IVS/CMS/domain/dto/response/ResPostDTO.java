package IVS.CMS.domain.dto.response;

import java.time.LocalDateTime;

import IVS.CMS.domain.constants.PostStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResPostDTO {
    private Long postId;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private PostStatusEnum status;

    private CategoryPost category;

    private LocalDateTime createdAt;
    private UserPost createdBy;
    private LocalDateTime updatedAt;
    private UserPost updatedBy;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryPost {
        private Long categoryId;
        private String categoryName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserPost {
        private Long id;
        private String fullname;
    }
}