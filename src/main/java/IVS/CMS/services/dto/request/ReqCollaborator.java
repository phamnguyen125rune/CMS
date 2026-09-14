package IVS.CMS.services.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCollaborator {
    private String collabName;
    private String description;
    private Integer position;
    private String companyImage;
    private Boolean visible;
}
