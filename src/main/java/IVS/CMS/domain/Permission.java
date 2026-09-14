package IVS.CMS.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission {
    private Long permissionId;
    private Long actionId;
    private Long apiId;

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    private Action action;
    private Api api;
}