package IVS.CMS.controllers;

import IVS.CMS.domain.FormCategory;
import IVS.CMS.services.FormCategoryService;
import IVS.CMS.services.dto.request.ReqCreateFormCategoryDTO;
import IVS.CMS.services.dto.request.ReqUpdateFormCategoryDTO;
import IVS.CMS.services.dto.response.RestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/form-categories")
public class FormCategoryController {

    private final FormCategoryService formCategoryService;

    public FormCategoryController(FormCategoryService formCategoryService) {
        this.formCategoryService = formCategoryService;
    }

    @PostMapping
    public ResponseEntity<RestResponse<FormCategory>> createCategory(@Valid @RequestBody ReqCreateFormCategoryDTO dto) {
        FormCategory category = formCategoryService.createCategory(dto);
        RestResponse<FormCategory> response = new RestResponse<>();
        response.setData(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestResponse<FormCategory>> updateCategory(
            @PathVariable("id") Long id, 
            @Valid @RequestBody ReqUpdateFormCategoryDTO dto) {
        FormCategory category = formCategoryService.updateCategory(id, dto);
        RestResponse<FormCategory> response = new RestResponse<>();
        response.setData(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<FormCategory>>> getAllCategories() {
        List<FormCategory> categories = formCategoryService.getAllCategories();
        RestResponse<List<FormCategory>> response = new RestResponse<>();
        response.setData(categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<FormCategory>> getCategoryById(@PathVariable("id") Long id) {
        FormCategory category = formCategoryService.getCategoryById(id);
        RestResponse<FormCategory> response = new RestResponse<>();
        response.setData(category);
        return ResponseEntity.ok(response);
    }

@DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteCategory(@PathVariable("id") Long id) {
        RestResponse<Void> response = new RestResponse<>();
        try {
            formCategoryService.deleteCategory(id);
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            System.out.println("Error deleting category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}