package IVS.CMS.services.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResCollaboratorSetting {

    private Long settingId;
    private Integer columnsPerRow;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}