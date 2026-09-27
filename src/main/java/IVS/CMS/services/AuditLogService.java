package IVS.CMS.services;

import IVS.CMS.services.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.services.dto.response.ResAuditLogSearchDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;

public interface AuditLogService {
    ResAuditLogSearchDTO searchAuditLogs(ReqAuditLogFilterDTO filter, long requestStartTime);
    ResultPaginationDTO getAllAuditLogs(ReqAuditLogFilterDTO filter, int page, int pageSize);
    byte[] exportToCsv(ReqAuditLogFilterDTO filter, int limit);
}
