package IVS.CMS.services.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ResBannerDTO {
    private Long bannerId;
    private String title;
    private String highlightText;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String mobileImageUrl;
    private String primaryBtnText;
    private String primaryBtnUrl;
    private String secondaryBtnText;
    private String secondaryBtnUrl;
    private String statsJson;
    private String floatingBadgeText;
    private String position;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}
