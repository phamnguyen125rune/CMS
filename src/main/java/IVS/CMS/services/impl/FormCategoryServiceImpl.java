package IVS.CMS.services.impl;

import IVS.CMS.domain.FormCategory;
import IVS.CMS.repositories.FormCategoryRepository;
import IVS.CMS.services.FormCategoryService;
import IVS.CMS.services.dto.request.ReqCreateFormCategoryDTO;
import IVS.CMS.services.dto.request.ReqUpdateFormCategoryDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormCategoryServiceImpl implements FormCategoryService {

    private final FormCategoryRepository repository;

    public FormCategoryServiceImpl(FormCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public FormCategory createCategory(ReqCreateFormCategoryDTO dto) {
        FormCategory category = new FormCategory();
        category.setCategoryName(dto.getCategoryName());
        category.setCreatedBy(dto.getCreatedBy());
        return repository.save(category);
    }

    @Override
    public FormCategory updateCategory(Long id, ReqUpdateFormCategoryDTO dto) {
        FormCategory category = getCategoryById(id);
        category.setCategoryName(dto.getCategoryName());
        category.setUpdatedBy(dto.getUpdatedBy());
        return repository.update(category);
    }

    @Override
    public FormCategory getCategoryById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
    }

    @Override
    public List<FormCategory> getAllCategories() {
        return repository.findAll();
    }

    @Override
    public void deleteCategory(Long id) {
        // Lưu ý: Có thể bạn cần kiểm tra xem Category này đã có FormDetail nào sử dụng chưa trước khi xóa.
        FormCategory category = getCategoryById(id);
        repository.deleteById(category.getFormCategoryId());
    }
}