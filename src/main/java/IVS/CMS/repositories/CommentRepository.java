package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;
import IVS.CMS.domain.Comment;
import IVS.CMS.services.dto.response.ResCommentDTO;

public interface CommentRepository {
    Comment save(Comment comment);

    Optional<Comment> findById(long commentId);

    void delete(long commentId);

    void updateStatus(long commentId, String status, Long updatedBy);

    List<ResCommentDTO> findFlatCommentsByPostId(long postId, String status);
}