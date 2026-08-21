package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import IVS.CMS.domain.PostCategory;

public interface CategoryRepository {
    PostCategory save(PostCategory category);

    Optional<PostCategory> findById(long id);

    List<PostCategory> findAll();

    void delete(long id);

    boolean existsByName(String categoryName);

    boolean existsByNameForUpdate(long id, String categoryName);
}