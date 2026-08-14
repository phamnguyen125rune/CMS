package IVS.CMS.services;

import java.util.List;
import IVS.CMS.domain.Category;

public interface CategoryService {
    Category createCategory(Category category);

    Category updateCategory(long id, Category category);

    Category fetchById(long id);

    List<Category> fetchAll();

    void deleteCategory(long id);
}