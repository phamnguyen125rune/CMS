package IVS.CMS.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.CmsRecord;
import IVS.CMS.domain.dto.request.ReqCmsRecordDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.CmsRecordRepository;
import IVS.CMS.services.CmsRecordService;
import IVS.CMS.services.SecurityService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CmsRecordServiceImpl implements CmsRecordService {
    private final CmsRecordRepository repository;

    @Override
    public ResultPaginationDTO findAll(String moduleKey, String search, String status, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 200));
        int offset = (safePage - 1) * safeSize;

        long total = repository.count(moduleKey, search, status);
        List<CmsRecord> records = repository.findAll(moduleKey, search, status, safeSize, offset);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(safePage);
        meta.setPageSize(safeSize);
        meta.setTotal(total);
        meta.setPages((int) Math.ceil((double) total / safeSize));

        ResultPaginationDTO response = new ResultPaginationDTO();
        response.setMeta(meta);
        response.setResult(records);
        return response;
    }

    @Override
    public List<CmsRecord> findPublished(String moduleKey, int page, int size) {
        if (isBlank(moduleKey)) {
            throw new BadRequestException("Module không được để trống");
        }
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        return repository.findPublished(moduleKey.trim(), safeSize, (safePage - 1) * safeSize);
    }

    @Override
    public CmsRecord findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dữ liệu CMS"));
    }

    @Override
    @Transactional
    public CmsRecord create(ReqCmsRecordDTO req) {
        CmsRecord record = new CmsRecord();
        apply(record, req);
        return repository.save(record);
    }

    @Override
    @Transactional
    public CmsRecord update(long id, ReqCmsRecordDTO req) {
        CmsRecord record = findById(id);
        apply(record, req);
        return repository.save(record);
    }

    @Override
    @Transactional
    public CmsRecord updateStatus(long id, String status) {
        String normalizedStatus = normalizeStatus(status);
        int updated = repository.updateStatus(
                id,
                normalizedStatus,
                SecurityService.getCurrentUserLogin().orElse("system"));
        if (updated != 1) {
            throw new ResourceNotFoundException("Không tìm thấy dữ liệu CMS");
        }
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(long id) {
        int updated = repository.softDelete(id, SecurityService.getCurrentUserLogin().orElse("system"));
        if (updated != 1) {
            throw new ResourceNotFoundException("Không tìm thấy dữ liệu CMS");
        }
    }

    private void apply(CmsRecord record, ReqCmsRecordDTO req) {
        record.setModuleKey(req.getModuleKey().trim());
        record.setTitle(req.getTitle().trim());
        record.setSubtitle(trimToNull(req.getSubtitle()));
        record.setType(isBlank(req.getType()) ? "General" : req.getType().trim());
        record.setStatus(normalizeStatus(isBlank(req.getStatus()) ? "ACTIVE" : req.getStatus()));
        record.setOwner(isBlank(req.getOwner()) ? "Admin" : req.getOwner().trim());
        record.setDescription(trimToNull(req.getDescription()));
        record.setImageUrl(trimToNull(req.getImageUrl()));
    }

    private String normalizeStatus(String status) {
        if (isBlank(status)) {
            throw new BadRequestException("Trạng thái không được để trống");
        }
        return status.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
