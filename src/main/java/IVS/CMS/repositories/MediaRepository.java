package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import IVS.CMS.domain.Media;

@Repository
public interface MediaRepository  {
    Media save(Media Media);

    Optional<Media> findById(long id);

    List<Media> findAll();

    List<Media> search(String keyword);

    long count();

    void delete(Media Media);
}
