package IVS.CMS.repositories;

import IVS.CMS.domain.FormCategory;
import java.util.List;
import java.util.Optional;

public interface FormCategoryRepository {
    FormCategory save(FormCategory category);
    FormCategory update(FormCategory category);
    Optional<FormCategory> findById(Long id);
    List<FormCategory> findAll();
    void deleteById(Long id);
}