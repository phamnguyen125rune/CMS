package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.PostReview;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.constants.PostStatusEnum;
import IVS.CMS.services.dto.request.ReqPostReviewDTO;
import IVS.CMS.services.dto.request.ReqPostReviewUpdateDTO;
import IVS.CMS.services.dto.response.ResPostReviewDTO;
import IVS.CMS.repositories.PostRepository;
import IVS.CMS.repositories.PostReviewRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.PostReviewService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ForbiddenException;
import IVS.CMS.services.error.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostReviewServiceImpl implements PostReviewService {

    private final PostReviewRepository postReviewRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public PostReview reviewPost(long postId, ReqPostReviewDTO req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));

        Long currentUserId = SecurityService.getCurrentUserId().orElse(null);

        PostReview review = new PostReview();
        review.setPostId(postId);
        review.setReviewerId(currentUserId);
        review.setAction(req.getAction());
        review.setComment(req.getComment());
        review.setCreatedAt(LocalDateTime.now());

        PostReview savedReview = postReviewRepository.save(review);

        String newStatus;
        switch (req.getAction()) {
            case APPROVED:
                newStatus = PostStatusEnum.APPROVED.name();
                break;
            case PUBLISHED:
                newStatus = PostStatusEnum.PUBLISHED.name();
                break;
            case REJECTED:
                newStatus = PostStatusEnum.REJECTED.name();
                break;
            case UNPUBLISHED:
                newStatus = PostStatusEnum.UNPUBLISHED.name();
                break;
            default:
                newStatus = post.getStatus().name();
        }
        postRepository.updateStatus(postId, newStatus, currentUserId);

        return savedReview;
    }

    @Override
    public List<ResPostReviewDTO> getReviewsByPostId(long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        return postReviewRepository.findDTOByPostId(postId);
    }

    @Override
    public PostReview getReviewById(long postId, long reviewId) {
        PostReview review = postReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Lượt đánh giá không tồn tại"));

        if (review.getPostId() != postId) {
            throw new BadRequestException("Lượt đánh giá này không thuộc về bài viết được yêu cầu");
        }
        return review;
    }

    @Override
    @Transactional
    public PostReview updateReview(long postId, long reviewId, ReqPostReviewUpdateDTO req) {
        PostReview review = this.getReviewById(postId, reviewId);
        Long currentUserId = SecurityService.getCurrentUserId().orElse(null);

        if (currentUserId == null || !currentUserId.equals(review.getReviewerId())) {
            throw new ForbiddenException("Bạn không có quyền sửa lượt đánh giá của người khác");
        }

        review.setComment(req.getComment());
        review.setUpdatedAt(LocalDateTime.now());
        review.setUpdatedBy(currentUserId);

        return postReviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(long postId, long reviewId) {
        PostReview review = this.getReviewById(postId, reviewId);
        Long currentUserId = SecurityService.getCurrentUserId().orElse(null);

        if (currentUserId == null || !currentUserId.equals(review.getReviewerId())) {
            throw new ForbiddenException("Bạn không có quyền xóa lượt đánh giá của người khác");
        }

        postReviewRepository.delete(reviewId);
    }
}