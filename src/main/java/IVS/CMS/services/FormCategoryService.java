package IVS.CMS.services;

import IVS.CMS.domain.FormCategory;
import IVS.CMS.services.dto.request.ReqCreateFormCategoryDTO;
import IVS.CMS.services.dto.request.ReqUpdateFormCategoryDTO;
import java.util.List;

public interface FormCategoryService {
    FormCategory createCategory(ReqCreateFormCategoryDTO dto);
    FormCategory updateCategory(Long id, ReqUpdateFormCategoryDTO dto);
    FormCategory getCategoryById(Long id);
    List<FormCategory> getAllCategories();
    void deleteCategory(Long id);
}