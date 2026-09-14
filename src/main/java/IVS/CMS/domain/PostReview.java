package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import IVS.CMS.domain.constants.PostReviewActionEnum;

@Getter
@Setter
public class PostReview {
    private Long reviewId;
    private Long postId;
    private Long reviewerId;
    private PostReviewActionEnum action;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}