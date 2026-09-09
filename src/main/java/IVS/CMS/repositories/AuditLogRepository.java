package IVS.CMS.repositories;

import java.util.List;
import IVS.CMS.domain.AuditLog;
import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResAuditLogDTO;

public interface AuditLogRepository {
    void save(AuditLog log);
    List<ResAuditLogDTO> findAll(ReqAuditLogFilterDTO filter, int limit, int offset);
    long count(ReqAuditLogFilterDTO filter);
}
