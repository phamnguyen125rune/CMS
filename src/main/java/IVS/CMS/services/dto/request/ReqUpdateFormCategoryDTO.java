package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReqUpdateFormCategoryDTO {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    private Long updatedBy;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
}