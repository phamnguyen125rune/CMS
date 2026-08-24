package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Menu {
    private Long menuId;
    private String title;
    private String url;
    private Integer menuType;
    private Integer displayOrder;
    private Integer level;
    private Boolean visible;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}