package IVS.CMS.services;

import IVS.CMS.domain.PostReview;
import IVS.CMS.services.dto.request.ReqPostReviewDTO;
import IVS.CMS.services.dto.request.ReqPostReviewUpdateDTO;
import IVS.CMS.services.dto.response.ResPostReviewDTO;

import java.util.List;

public interface PostReviewService {
    PostReview reviewPost(long postId, ReqPostReviewDTO req);

    List<ResPostReviewDTO> getReviewsByPostId(long postId);

    PostReview getReviewById(long postId, long reviewId);

    PostReview updateReview(long postId, long reviewId, ReqPostReviewUpdateDTO req);

    void deleteReview(long postId, long reviewId);
}