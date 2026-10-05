package IVS.CMS.repositories;

import IVS.CMS.domain.Banner;
import java.util.List;
import java.util.Optional;

public interface BannerRepository {
    List<Banner> findActiveByPosition(String position);
    List<Banner> findAll(String search, String position, Boolean isActive, int page, int size);
    long count(String search, String position, Boolean isActive);
    Optional<Banner> findById(Long id);
    Banner save(Banner banner);
    Banner update(Banner banner);
    boolean delete(Long id);
    boolean updateStatus(Long id, boolean isActive);
}
