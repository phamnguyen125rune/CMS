package IVS.CMS.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollaboratorSetting {

    private Long settingId;
    private Integer columnsPerRow;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}