package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReqCreateFormCategoryDTO {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    // Tạm thời để user truyền lên, thực tế có thể lấy từ Token/Session của người đang đăng nhập
    private Long createdBy; 

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}