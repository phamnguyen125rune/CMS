package IVS.CMS.repositories;

import IVS.CMS.domain.Contact;
import java.util.List;
import java.util.Optional;

public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(Long id);
    List<Contact> findAll(String search, String status, int page, int size);
    long count(String search, String status);
    void updateStatus(Long id, String status);
    void updateReply(Long id, String replyMessage, String status);
    void deleteById(Long id);
}
