package IVS.CMS.audit.events;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import IVS.CMS.audit.repositories.AuditLogRepository;
import IVS.CMS.audit.utils.AuditDataSanitizer;
import IVS.CMS.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Lắng nghe AuditLogEvent và ghi log vào cơ sở dữ liệu ở luồng ngầm.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private final AuditLogRepository auditLogRepository;
    private final AuditDataSanitizer sanitizer;

    @Async
    @EventListener
    public void onAuditLogEvent(AuditLogEvent event) {
        try {
            AuditLog logEntity = new AuditLog();
            logEntity.setUserId(event.getUserId());
            logEntity.setEntityType(event.getEntityType() != null ? event.getEntityType() : "UNKNOWN");
            logEntity.setEntityId(event.getEntityId() != null ? event.getEntityId().intValue() : 0);
            logEntity.setAction(event.getAction() != null ? event.getAction() : "UNKNOWN");

            logEntity.setOldValue(sanitizer.sanitizeAndSerialize(event.getRequestData()));
            logEntity.setNewValue(sanitizer.sanitizeAndSerialize(event.getResponseData()));

            logEntity.setStatusCode(event.getStatusCode() != null ? event.getStatusCode() : 500);
            logEntity.setCreatedAt(LocalDateTime.now());

            auditLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
