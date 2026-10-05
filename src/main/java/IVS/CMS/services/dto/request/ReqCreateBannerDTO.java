package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateBannerDTO {

    @NotBlank(message = "Tiêu đề banner không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 255, message = "Highlight text không được vượt quá 255 ký tự")
    private String highlightText;

    @Size(max = 255, message = "Subtitle/Badge không được vượt quá 255 ký tự")
    private String subtitle;

    private String description;

    @NotBlank(message = "Hình ảnh banner không được để trống")
    @Size(max = 500, message = "Đường dẫn ảnh không được vượt quá 500 ký tự")
    private String imageUrl;

    @Size(max = 500, message = "Đường dẫn ảnh mobile không được vượt quá 500 ký tự")
    private String mobileImageUrl;

    @Size(max = 100, message = "Tên nút chính không được vượt quá 100 ký tự")
    private String primaryBtnText;

    @Size(max = 255, message = "Link nút chính không được vượt quá 255 ký tự")
    private String primaryBtnUrl;

    @Size(max = 100, message = "Tên nút phụ không được vượt quá 100 ký tự")
    private String secondaryBtnText;

    @Size(max = 255, message = "Link nút phụ không được vượt quá 255 ký tự")
    private String secondaryBtnUrl;

    private String statsJson;

    @Size(max = 255, message = "Badge nổi không được vượt quá 255 ký tự")
    private String floatingBadgeText;

    @Size(max = 50, message = "Vị trí không được vượt quá 50 ký tự")
    private String position = "HOME_HERO";

    private Integer displayOrder = 0;

    private Boolean isActive = true;
}
