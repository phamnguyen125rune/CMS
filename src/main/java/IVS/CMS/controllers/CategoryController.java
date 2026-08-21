package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.PostCategory;
import IVS.CMS.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('categories:EDIT')")
    public ResponseEntity<PostCategory> createCategory(@Valid @RequestBody PostCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.categoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('categories:EDIT')")
    public ResponseEntity<PostCategory> updateCategory(@PathVariable("id") long id,
            @Valid @RequestBody PostCategory category) {
        return ResponseEntity.ok(this.categoryService.updateCategory(id, category));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('categories:VIEW')")
    public ResponseEntity<PostCategory> getCategoryById(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.categoryService.fetchById(id));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PostCategory>> getAllCategories() {
        return ResponseEntity.ok(this.categoryService.fetchAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('categories:EDIT')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") long id) {
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}