package IVS.CMS.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import IVS.CMS.domain.Comment;
import IVS.CMS.services.CommentService;
import IVS.CMS.services.dto.request.ReqCommentCreateDTO;
import IVS.CMS.services.dto.request.ReqCommentUpdateDTO;
import IVS.CMS.services.dto.response.ResCommentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<ResCommentDTO>> getPostComments(
            @PathVariable("postId") long postId,
            @RequestParam(value = "status", defaultValue = "approved") String status) {
        return ResponseEntity.ok(commentService.getCommentTreeByPostId(postId, status));
    }

    @PostMapping("/comments")
    public ResponseEntity<Comment> createComment(@Valid @RequestBody ReqCommentCreateDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(req));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("id") long id,
            @Valid @RequestBody ReqCommentUpdateDTO req) {
        return ResponseEntity.ok(commentService.updateComment(id, req));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable("id") long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/comments/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable("id") long id,
            @RequestBody Map<String, String> body) {
        commentService.changeStatus(id, body.get("status"));
        return ResponseEntity.ok().build();
    }
}