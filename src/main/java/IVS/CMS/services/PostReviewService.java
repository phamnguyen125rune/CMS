package IVS.CMS.services;

import IVS.CMS.domain.PostReview;
import IVS.CMS.domain.dto.request.ReqPostReviewDTO;
import IVS.CMS.domain.dto.request.ReqPostReviewUpdateDTO;
import IVS.CMS.domain.dto.response.ResPostReviewDTO;

import java.util.List;

public interface PostReviewService {
    PostReview reviewPost(long postId, ReqPostReviewDTO req);

    List<ResPostReviewDTO> getReviewsByPostId(long postId);

    PostReview getReviewById(long postId, long reviewId);

    PostReview updateReview(long postId, long reviewId, ReqPostReviewUpdateDTO req);

    void deleteReview(long postId, long reviewId);
}