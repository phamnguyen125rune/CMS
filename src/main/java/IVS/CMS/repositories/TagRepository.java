package IVS.CMS.repositories;

import IVS.CMS.domain.Tag;
import java.util.List;

public interface TagRepository {
    List<Tag> findAll();
    void save(Tag tag);
    void deleteById(Long id);
}