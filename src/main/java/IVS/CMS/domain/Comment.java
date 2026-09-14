package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {
    private Long commentId;
    private Long postId;
    private Long parentId;

    private String commentText;
    private String imageUrl;

    private String status;

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}