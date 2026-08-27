package IVS.CMS.audit.events;

import lombok.Getter;

@Getter
public class AuditLogEvent {

    private final Long userId;
    private final String entityType;
    private final Long entityId;
    private final String action;
    private final Object requestData;
    private final Object responseData;
    private final Integer statusCode;

    public AuditLogEvent(Long userId, String entityType, Long entityId, 
                         String action, Object requestData, Object responseData, Integer statusCode) {
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.requestData = requestData;
        this.responseData = responseData;
        this.statusCode = statusCode != null ? statusCode : 200;
    }
}
