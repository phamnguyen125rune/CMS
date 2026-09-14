package IVS.CMS.services.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonRawValue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAuditLogDTO {
    private Long logId;
    private Long userId;
    private String entityType;
    private Integer entityId;
    private String action;
    @JsonRawValue
    private String oldValue;
    @JsonRawValue
    private String newValue;
    private LocalDateTime createdAt;
    private Integer statusCode;
}
