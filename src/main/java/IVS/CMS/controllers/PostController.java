package IVS.CMS.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import IVS.CMS.domain.dto.request.ReqPostCreateDTO;
import IVS.CMS.domain.dto.request.ReqPostFilterDTO;
import IVS.CMS.domain.dto.request.ReqPostUpdateDTO;
import IVS.CMS.domain.dto.response.ResPostDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ResPostDTO> createPost(@Valid @RequestBody ReqPostCreateDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.postService.createPost(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResPostDTO> updatePost(
            @PathVariable("id") long id,
            @Valid @RequestBody ReqPostUpdateDTO req) {
        return ResponseEntity.ok(this.postService.updatePost(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResPostDTO> getPostById(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.postService.getPostById(id));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllPosts(
            @ModelAttribute ReqPostFilterDTO filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(this.postService.getAllPosts(filter, page, pageSize));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") long id) {
        this.postService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable("id") long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        this.postService.changeStatus(id, status);
        return ResponseEntity.ok().build();
    }
}