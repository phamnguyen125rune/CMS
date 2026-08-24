package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.CmsRecord;
import IVS.CMS.domain.dto.request.ReqCmsRecordDTO;
import IVS.CMS.domain.dto.request.ReqCmsStatusDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.services.CmsRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CmsRecordController {
    private final CmsRecordService cmsRecordService;

    @GetMapping("/cms-records")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResultPaginationDTO> findAll(
            @RequestParam(value = "moduleKey", required = false) String moduleKey,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", defaultValue = "ALL") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(cmsRecordService.findAll(moduleKey, search, status, page, size));
    }

    @GetMapping("/cms-records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CmsRecord> findById(@PathVariable("id") long id) {
        return ResponseEntity.ok(cmsRecordService.findById(id));
    }

    @PostMapping("/cms-records")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CmsRecord> create(@Valid @RequestBody ReqCmsRecordDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cmsRecordService.create(req));
    }

    @PutMapping("/cms-records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CmsRecord> update(
            @PathVariable("id") long id,
            @Valid @RequestBody ReqCmsRecordDTO req) {
        return ResponseEntity.ok(cmsRecordService.update(id, req));
    }

    @PutMapping("/cms-records/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CmsRecord> updateStatus(
            @PathVariable("id") long id,
            @Valid @RequestBody ReqCmsStatusDTO req) {
        return ResponseEntity.ok(cmsRecordService.updateStatus(id, req.getStatus()));
    }

    @DeleteMapping("/cms-records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        cmsRecordService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/public/cms-records")
    public ResponseEntity<List<CmsRecord>> findPublished(
            @RequestParam("moduleKey") String moduleKey,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(cmsRecordService.findPublished(moduleKey, page, size));
    }
}
