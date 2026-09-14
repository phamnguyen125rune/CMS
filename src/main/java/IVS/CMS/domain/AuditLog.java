package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLog {
    private Long logId;
    private Long userId;
    private String entityType;
    private Integer entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
    private Integer statusCode;
}