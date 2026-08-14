package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class Category {

    private Long categoryId;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime lastUpdatedAt;
    private Long lastUpdatedBy;
}