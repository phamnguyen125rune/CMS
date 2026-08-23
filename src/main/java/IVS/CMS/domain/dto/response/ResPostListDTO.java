package IVS.CMS.domain.dto.response;

import java.time.LocalDateTime;
import IVS.CMS.domain.constants.PostStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPostListDTO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private PostStatusEnum status;
    private ResPostDTO.CategoryInfo category;
    private ResPostDTO.AuthorInfo author;
    private Long featuredMediaId;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}