package IVS.CMS.controllers;

import IVS.CMS.domain.PostReview;
import IVS.CMS.services.dto.request.ReqPostReviewDTO;
import IVS.CMS.services.dto.request.ReqPostReviewUpdateDTO;
import IVS.CMS.services.dto.response.ResPostReviewDTO;
import IVS.CMS.services.PostReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/reviews")
@RequiredArgsConstructor
public class PostReviewController {

    private final PostReviewService postReviewService;

    @PostMapping
    public ResponseEntity<PostReview> reviewPost(
            @PathVariable("postId") long postId,
            @Valid @RequestBody ReqPostReviewDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.postReviewService.reviewPost(postId, req));
    }

    @GetMapping
    public ResponseEntity<List<ResPostReviewDTO>> getPostReviews(@PathVariable("postId") long postId) {
        return ResponseEntity.ok(this.postReviewService.getReviewsByPostId(postId));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<PostReview> getReviewById(
            @PathVariable("postId") long postId,
            @PathVariable("reviewId") long reviewId) {
        return ResponseEntity.ok(this.postReviewService.getReviewById(postId, reviewId));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<PostReview> updateReview(
            @PathVariable("postId") long postId,
            @PathVariable("reviewId") long reviewId,
            @Valid @RequestBody ReqPostReviewUpdateDTO req) {
        return ResponseEntity.ok(this.postReviewService.updateReview(postId, reviewId, req));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("postId") long postId,
            @PathVariable("reviewId") long reviewId) {
        this.postReviewService.deleteReview(postId, reviewId);
        return ResponseEntity.noContent().build();
    }
}