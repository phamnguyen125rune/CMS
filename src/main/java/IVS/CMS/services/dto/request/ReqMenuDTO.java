package IVS.CMS.services.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqMenuDTO {
    private Long parentId;
    private String title;
    private String url;
    private Integer displayOrder;
    private Integer level;
    private Boolean visible;
}