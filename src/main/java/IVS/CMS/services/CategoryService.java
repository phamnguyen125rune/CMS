package IVS.CMS.services;

import java.util.List;
import IVS.CMS.domain.PostCategory;

public interface CategoryService {
    PostCategory createCategory(PostCategory category);

    PostCategory updateCategory(long id, PostCategory category);

    PostCategory fetchById(long id);

    List<PostCategory> fetchAll();

    void deleteCategory(long id);
}