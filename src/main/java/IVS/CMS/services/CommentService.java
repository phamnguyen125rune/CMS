package IVS.CMS.services;

import java.util.List;
import IVS.CMS.domain.Comment;
import IVS.CMS.services.dto.request.ReqCommentCreateDTO;
import IVS.CMS.services.dto.request.ReqCommentUpdateDTO;
import IVS.CMS.services.dto.response.ResCommentDTO;

public interface CommentService {
    Comment createComment(ReqCommentCreateDTO req);

    Comment updateComment(long commentId, ReqCommentUpdateDTO req);

    void deleteComment(long commentId);

    void changeStatus(long commentId, String status);

    List<ResCommentDTO> getCommentTreeByPostId(long postId, String status);
}