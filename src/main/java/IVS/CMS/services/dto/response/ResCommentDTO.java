package IVS.CMS.services.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCommentDTO {
    private Long commentId;
    private Long postId;
    private Long parentId;
    private String commentText;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AuthorInfo author;

    private List<ResCommentDTO> replies = new ArrayList<>();

    @Getter
    @Setter
    public static class AuthorInfo {
        private Long userId;
        private String fullName;
        private String avatarUrl;
    }
}