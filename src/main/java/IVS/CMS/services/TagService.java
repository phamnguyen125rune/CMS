package IVS.CMS.services;

import IVS.CMS.domain.Tag;
import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    void createTag(Tag tag);
    void deleteTag(Long id);
}