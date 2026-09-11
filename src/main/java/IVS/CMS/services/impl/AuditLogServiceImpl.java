package IVS.CMS.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResAuditLogDTO;
import IVS.CMS.domain.dto.response.ResAuditLogSearchDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.AuditLogRepository;
import IVS.CMS.services.AuditLogService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ResAuditLogSearchDTO searchAuditLogs(ReqAuditLogFilterDTO filter, long requestStartTime) {
        if (filter == null) {
            filter = new ReqAuditLogFilterDTO();
        }

        int page = (filter.getPage() != null && filter.getPage() >= 1) ? filter.getPage() : 1;
        int size = (filter.getSize() != null && filter.getSize() >= 1) ? Math.min(filter.getSize(), 100) : 20;
        int offset = (page - 1) * size;

        ResAuditLogSearchDTO.SummaryStats summary = this.auditLogRepository.aggregateMetrics(filter);
        List<ResAuditLogDTO> items = this.auditLogRepository.findAll(filter, size, offset);

        long executionTimeMs = System.currentTimeMillis() - requestStartTime;
        if (summary != null) {
            summary.setExecutionTimeMs(executionTimeMs);
        }

        long total = (summary != null) ? summary.getTotalRecords() : 0L;
        int pages = (int) Math.ceil((double) total / size);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(size);
        meta.setPages(pages);
        meta.setTotal(total);

        return ResAuditLogSearchDTO.builder()
                .summary(summary)
                .meta(meta)
                .items(items)
                .build();
    }

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

    @Override
    public byte[] exportToCsv(ReqAuditLogFilterDTO filter, int limit) {
        List<ResAuditLogDTO> logs = this.auditLogRepository.findForExport(filter, limit);

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM Excel Vietnamese display support
        sb.append('\uFEFF');
        sb.append("Log ID,Thời gian,Mã User,Thực thể,ID Thực thể,Hành động,Mã HTTP\n");

        for (ResAuditLogDTO item : logs) {
            sb.append(escapeCsv(item.getLogId())).append(",");
            sb.append(escapeCsv(item.getCreatedAt() != null ? item.getCreatedAt().format(DATE_TIME_FORMATTER) : "")).append(",");
            sb.append(escapeCsv(item.getUserId())).append(",");
            sb.append(escapeCsv(item.getEntityType())).append(",");
            sb.append(escapeCsv(item.getEntityId())).append(",");
            sb.append(escapeCsv(item.getAction())).append(",");
            sb.append(escapeCsv(item.getStatusCode())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String str = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + str + "\"";
    }
}
