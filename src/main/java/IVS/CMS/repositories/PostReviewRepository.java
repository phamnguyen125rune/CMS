package IVS.CMS.repositories;

import IVS.CMS.domain.PostReview;
import IVS.CMS.domain.dto.response.ResPostReviewDTO;

import java.util.List;
import java.util.Optional;

public interface PostReviewRepository {
    PostReview save(PostReview postReview);

    Optional<PostReview> findById(long reviewId);

    List<ResPostReviewDTO> findDTOByPostId(long postId);

    void delete(long reviewId);
}