package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Category;
import IVS.CMS.repositories.CategoryRepository;
import IVS.CMS.services.CategoryService;
import IVS.CMS.services.SecurityService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        String categoryName = category.getCategoryName();

        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new BadRequestException("Tên danh mục không hợp lệ");
        }

        categoryName = categoryName.trim();

        if (this.categoryRepository.existsByName(categoryName)) {
            throw new BadRequestException("Danh mục '" + categoryName + "' đã tồn tại");
        }

        category.setCategoryName(categoryName);
        category.setCreatedAt(LocalDateTime.now());
        category.setCreatedBy(SecurityService.getCurrentUserId().orElse(null));

        return this.categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(long id, Category category) {
        Category currentCategory = this.fetchById(id);
        String newName = category.getCategoryName();

        if (newName == null || newName.trim().isEmpty()) {
            throw new BadRequestException("Tên danh mục không hợp lệ");
        }

        newName = newName.trim();
        if (this.categoryRepository.existsByNameForUpdate(id, newName)) {
            throw new BadRequestException("Danh mục '" + newName + "' đã tồn tại");
        }

        currentCategory.setCategoryName(newName);
        currentCategory.setLastUpdatedAt(LocalDateTime.now());
        currentCategory.setLastUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

        return this.categoryRepository.save(currentCategory);
    }

    @Override
    public Category fetchById(long id) {
        return this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục với id " + id + " không tồn tại"));
    }

    @Override
    public List<Category> fetchAll() {
        return this.categoryRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteCategory(long id) {
        Category currentCategory = this.fetchById(id);
        this.categoryRepository.delete(currentCategory.getCategoryId());
    }

}