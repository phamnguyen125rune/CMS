package IVS.CMS.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import IVS.CMS.domain.Comment;
import IVS.CMS.repositories.CommentRepository;
import IVS.CMS.repositories.PostRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.CommentService;
import IVS.CMS.services.dto.request.ReqCommentCreateDTO;
import IVS.CMS.services.dto.request.ReqCommentUpdateDTO;
import IVS.CMS.services.dto.response.ResCommentDTO;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ForbiddenException;
import IVS.CMS.services.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public Comment createComment(ReqCommentCreateDTO req) {
        Long currentUserId = SecurityService.getCurrentUserId()
                .orElseThrow(() -> new ForbiddenException("Vui lòng đăng nhập để bình luận"));

        postRepository.findById(req.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));

        if (req.getParentId() != null) {
            commentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bình luận cha không tồn tại"));
        }

        Comment comment = new Comment();
        comment.setPostId(req.getPostId());
        comment.setParentId(req.getParentId());
        comment.setCommentText(req.getCommentText());
        comment.setImageUrl(req.getImageUrl());
        comment.setStatus("approved");
        comment.setCreatedBy(currentUserId);
        comment.setUpdatedBy(currentUserId);

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment updateComment(long commentId, ReqCommentUpdateDTO req) {
        Long currentUserId = SecurityService.getCurrentUserId()
                .orElseThrow(() -> new ForbiddenException("Chưa đăng nhập"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại"));

        if (!comment.getCreatedBy().equals(currentUserId)) {
            throw new ForbiddenException("Bạn không có quyền sửa bình luận này");
        }

        comment.setCommentText(req.getCommentText());
        comment.setImageUrl(req.getImageUrl());
        comment.setUpdatedBy(currentUserId);

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(long commentId) {
        Long currentUserId = SecurityService.getCurrentUserId()
                .orElseThrow(() -> new ForbiddenException("Chưa đăng nhập"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại"));

        boolean isOwner = comment.getCreatedBy().equals(currentUserId);

        if (!isOwner) {
            throw new ForbiddenException("Bạn không có quyền xóa bình luận này");
        }

        commentRepository.delete(commentId);
    }

    @Override
    @Transactional
    public void changeStatus(long commentId, String status) {
        Long currentUserId = SecurityService.getCurrentUserId()
                .orElseThrow(() -> new ForbiddenException("Chưa đăng nhập"));

        if (!status.matches("^(pending|approved|rejected|spam)$")) {
            throw new BadRequestException("Trạng thái không hợp lệ");
        }

        commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận không tồn tại"));

        commentRepository.updateStatus(commentId, status, currentUserId);
    }

    @Override
    public List<ResCommentDTO> getCommentTreeByPostId(long postId, String status) {
        List<ResCommentDTO> flatComments = commentRepository.findFlatCommentsByPostId(postId, status);

        Map<Long, ResCommentDTO> map = new HashMap<>();
        List<ResCommentDTO> rootComments = new ArrayList<>();

        for (ResCommentDTO c : flatComments) {
            map.put(c.getCommentId(), c);
        }

        for (ResCommentDTO c : flatComments) {
            if (c.getParentId() == null) {
                rootComments.add(c);
            } else {
                ResCommentDTO parent = map.get(c.getParentId());
                if (parent != null) {
                    parent.getReplies().add(c);
                }
            }
        }
        return rootComments;
    }
}