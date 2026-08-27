package IVS.CMS.audit.repositories;

import IVS.CMS.domain.AuditLog;

public interface AuditLogRepository {
    void save(AuditLog log);
}
