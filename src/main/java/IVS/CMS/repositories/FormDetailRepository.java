package IVS.CMS.repositories;

import IVS.CMS.domain.FormDetail;
import java.util.List;
import java.util.Optional;

public interface FormDetailRepository {
    FormDetail save(FormDetail formDetail);
    Optional<FormDetail> findById(Long id);
    List<FormDetail> findAll(String search, String status, int page, int size);
    long count(String search, String status);
    void updateStatus(Long id, String status);
    void updateReply(Long id, String replyMessage, String status);
    void deleteById(Long id);
}