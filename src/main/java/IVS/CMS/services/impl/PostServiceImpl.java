package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.PostCategory;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.constants.PostStatusEnum;
import IVS.CMS.repositories.PostCategoryRepository;
import IVS.CMS.repositories.PostRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.PostService;
import IVS.CMS.services.dto.request.ReqPostCreateDTO;
import IVS.CMS.services.dto.request.ReqPostUpdateDTO;
import IVS.CMS.services.dto.response.ResPostDTO;
import IVS.CMS.services.dto.response.ResPostListDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.PostMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostCategoryRepository categoryRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResPostDTO createPost(ReqPostCreateDTO req) {
        if (this.postRepository.existsBySlug(req.getSlug())) {
            throw new BadRequestException("Đường dẫn (slug) '" + req.getSlug() + "' đã tồn tại");
        }
        PostCategory category = this.categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        Post post = this.postMapper.reqCreateToPost(req);
        post.setStatus(PostStatusEnum.DRAFT);
        post.setCreatedAt(LocalDateTime.now());
        post.setCreatedBy(SecurityService.getCurrentUserId().orElse(null));

        Post savedPost = this.postRepository.save(post);
        return this.postMapper.postToResPostDTO(savedPost, category);
    }

    @Override
    @Transactional
    public ResPostDTO updatePost(long id, ReqPostUpdateDTO req) {
        Post currentPost = this.postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));

        if (this.postRepository.existsBySlugForUpdate(id, req.getSlug())) {
            throw new BadRequestException("Đường dẫn (slug) '" + req.getSlug() + "' đã tồn tại");
        }

        PostCategory category = this.categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        Post tempPost = this.postMapper.reqUpdateToPost(req);
        currentPost.setTitle(tempPost.getTitle());
        currentPost.setSlug(tempPost.getSlug());
        currentPost.setSummary(tempPost.getSummary());
        currentPost.setContent(tempPost.getContent());
        currentPost.setCategoryId(tempPost.getCategoryId());

        if (tempPost.getStatus() != null) {
            currentPost.setStatus(tempPost.getStatus());
        }

        currentPost.setUpdatedAt(LocalDateTime.now());
        currentPost.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

        Post updatedPost = this.postRepository.save(currentPost);
        return this.postMapper.postToResPostDTO(updatedPost, category);
    }

    @Override
    public ResPostDTO getPostById(long id) {
        Post post = this.postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        PostCategory category = this.categoryRepository.findById(post.getCategoryId()).orElse(null);
        ResPostDTO res = this.postMapper.postToResPostDTO(post, category);

        if (post.getCreatedBy() != null) {
            this.userRepository.findById(post.getCreatedBy())
                    .ifPresent(u -> res.getCreatedBy().setFullname(u.getFullName()));
        }
        if (post.getUpdatedBy() != null) {
            this.userRepository.findById(post.getUpdatedBy())
                    .ifPresent(u -> res.getUpdatedBy().setFullname(u.getFullName()));
        }
        return res;
    }

    @Override
    public ResultPaginationDTO getAllPosts(int page, int pageSize) {
        if (page < 1)
            page = 1;
        if (pageSize < 1)
            pageSize = 10;

        long total = this.postRepository.count();
        int pages = (int) Math.ceil((double) total / pageSize);
        int offset = (page - 1) * pageSize;

        List<ResPostListDTO> listPostRes = this.postRepository.findAll(pageSize, offset);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(pageSize);
        meta.setPages(pages);
        meta.setTotal(total);

        ResultPaginationDTO res = new ResultPaginationDTO();
        res.setMeta(meta);
        res.setResult(listPostRes);

        return res;
    }

    @Override
    @Transactional
    public void deletePost(long id) {
        Post post = this.postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        this.postRepository.delete(post.getPostId());
    }

    @Override
    @Transactional
    public void changeStatus(long id, String status) {
        Post post = this.postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));
        try {
            PostStatusEnum newStatus = PostStatusEnum.valueOf(status.trim().toUpperCase());
            Long updatedBy = SecurityService.getCurrentUserId().orElse(null);
            this.postRepository.updateStatus(post.getPostId(), newStatus.name(), updatedBy);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Trạng thái bài viết không hợp lệ. Chấp nhận: DRAFT, PENDING, PUBLISHED,...");
        }
    }
}