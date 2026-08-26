package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import IVS.CMS.domain.Post;
import IVS.CMS.services.dto.request.ReqPostFilterDTO;
import IVS.CMS.services.dto.response.ResPostDTO;
import IVS.CMS.services.dto.response.ResPostListDTO;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(long id);

    List<ResPostListDTO> findAll(ReqPostFilterDTO filter, int limit, int offset);

    long count(ReqPostFilterDTO filter);

    void delete(long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugForUpdate(long id, String slug);

    void updateStatus(long id, String status, Long updatedBy);

    void addTagsToPost(long postId, List<Long> tagIds);

    void addMediaToPost(long postId, List<Long> mediaIds);

    void removeAllTagsFromPost(long postId);

    void removeAllMediaFromPost(long postId);

    List<ResPostDTO.TagInfo> getTagsByPostId(long postId);

    List<ResPostDTO.MediaInfo> getMediaByPostId(long postId);
}