package IVS.CMS.services.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMenuDTO {
    private Long menuId;
    private Long parentId;
    private String title;
    private String url;
    private Integer displayOrder;
    private Integer level;
    private Boolean visible;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}