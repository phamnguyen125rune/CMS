package IVS.CMS.services.impl;

import java.util.List;
import org.springframework.stereotype.Service;

import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResAuditLogDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.AuditLogRepository;
import IVS.CMS.services.AuditLogService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public ResultPaginationDTO getAllAuditLogs(ReqAuditLogFilterDTO filter, int page, int pageSize) {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }

        long total = this.auditLogRepository.count(filter);
        int pages = (int) Math.ceil((double) total / pageSize);
        int offset = (page - 1) * pageSize;

        List<ResAuditLogDTO> listAuditLogRes = this.auditLogRepository.findAll(filter, pageSize, offset);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(pageSize);
        meta.setPages(pages);
        meta.setTotal(total);

        ResultPaginationDTO res = new ResultPaginationDTO();
        res.setMeta(meta);
        res.setResult(listAuditLogRes);

        return res;
    }
}
