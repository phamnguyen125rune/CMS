package IVS.CMS.services.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCollaborator {

    private Long collabId;
    private String collabName;
    private String description;
    private Integer position;
    private String companyImage;
    private Boolean visible;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}