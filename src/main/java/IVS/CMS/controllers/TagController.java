package IVS.CMS.controllers;

import IVS.CMS.domain.Tag;
import IVS.CMS.services.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('tag', 'VIEW')")
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('tag', 'CREATE')")
    public ResponseEntity<Void> createTag(@RequestBody Tag tag) {
        tagService.createTag(tag);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('tag', 'DELETE')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok().build();
    }
}