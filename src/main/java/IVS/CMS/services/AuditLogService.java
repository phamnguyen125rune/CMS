package IVS.CMS.services;

import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;

public interface AuditLogService {
    ResultPaginationDTO getAllAuditLogs(ReqAuditLogFilterDTO filter, int page, int pageSize);
}
