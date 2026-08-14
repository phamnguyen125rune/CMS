package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import IVS.CMS.domain.Category;

public interface CategoryRepository {
    Category save(Category category);

    Optional<Category> findById(long id);

    List<Category> findAll();

    void delete(long id);

    boolean existsByName(String categoryName);

    boolean existsByNameForUpdate(long id, String categoryName);
}