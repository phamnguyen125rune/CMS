package IVS.CMS.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.services.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.services.dto.response.ResAuditLogSearchDTO;
import IVS.CMS.services.AuditLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ResAuditLogSearchDTO> getAllAuditLogs(
            @ModelAttribute ReqAuditLogFilterDTO filter) {
        long startTime = System.currentTimeMillis();
        return ResponseEntity.ok(this.auditLogService.searchAuditLogs(filter, startTime));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @ModelAttribute ReqAuditLogFilterDTO filter,
            @RequestParam(value = "limit", defaultValue = "5000") int limit) {
        byte[] csvData = this.auditLogService.exportToCsv(filter, limit);
        String filename = "audit_logs_export_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
