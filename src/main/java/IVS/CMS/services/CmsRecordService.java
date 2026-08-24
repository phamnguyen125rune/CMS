package IVS.CMS.services;

import java.util.List;

import IVS.CMS.domain.CmsRecord;
import IVS.CMS.domain.dto.request.ReqCmsRecordDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;

public interface CmsRecordService {
    ResultPaginationDTO findAll(String moduleKey, String search, String status, int page, int size);

    List<CmsRecord> findPublished(String moduleKey, int page, int size);

    CmsRecord findById(long id);

    CmsRecord create(ReqCmsRecordDTO req);

    CmsRecord update(long id, ReqCmsRecordDTO req);

    CmsRecord updateStatus(long id, String status);

    void delete(long id);
}
