package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import IVS.CMS.domain.Post;
import IVS.CMS.services.dto.response.ResPostListDTO;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(long id);

    List<ResPostListDTO> findAll(int limit, int offset);

    long count();

    void delete(long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugForUpdate(long id, String slug);

    void updateStatus(long id, String status, Long updatedBy);
}