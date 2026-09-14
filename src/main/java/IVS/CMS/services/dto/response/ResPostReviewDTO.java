package IVS.CMS.services.dto.response;

import java.time.LocalDateTime;
import IVS.CMS.domain.constants.PostReviewActionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResPostReviewDTO {
    private Long reviewId;
    private Long postId;
    private ReviewerInfo reviewer;
    private PostReviewActionEnum action;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewerInfo {
        private Long id;
        private String fullName;
        private String avatarUrl;
    }
}