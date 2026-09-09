package IVS.CMS.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.services.AuditLogService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllAuditLogs(
            @ModelAttribute ReqAuditLogFilterDTO filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(this.auditLogService.getAllAuditLogs(filter, page, pageSize));
    }
}
