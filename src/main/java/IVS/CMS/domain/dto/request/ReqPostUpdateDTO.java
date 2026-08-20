package IVS.CMS.domain.dto.request;

import IVS.CMS.domain.constants.PostStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqPostUpdateDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Slug (đường dẫn) không được để trống")
    private String slug;

    private String summary;

    @NotBlank(message = "Nội dung bài viết không được để trống")
    private String content;

    @NotNull(message = "Danh mục bài viết không được để trống")
    private Long categoryId;

    private PostStatusEnum status;
}