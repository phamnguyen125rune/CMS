package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;

import IVS.CMS.domain.CmsRecord;

public interface CmsRecordRepository {
    CmsRecord save(CmsRecord record);

    Optional<CmsRecord> findById(long id);

    List<CmsRecord> findAll(String moduleKey, String search, String status, int limit, int offset);

    long count(String moduleKey, String search, String status);

    List<CmsRecord> findPublished(String moduleKey, int limit, int offset);

    int updateStatus(long id, String status, String updatedBy);

    int softDelete(long id, String deletedBy);
}
