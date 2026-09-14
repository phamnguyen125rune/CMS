package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Collaborator {

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