package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import IVS.CMS.domain.Menu;

@Repository
public interface MenuRepository {
    List<Menu> getMenu();

    Optional<Menu> findById(Long id);

    int save(Menu menu);

    int update(Menu menu);

    int deleteById(Long id);
}