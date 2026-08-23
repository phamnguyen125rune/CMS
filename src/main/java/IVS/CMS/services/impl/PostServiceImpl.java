package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.PostCategory;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.User;
import IVS.CMS.domain.constants.PostStatusEnum;
import IVS.CMS.domain.dto.request.ReqPostCreateDTO;
import IVS.CMS.domain.dto.request.ReqPostUpdateDTO;
import IVS.CMS.domain.dto.response.ResPostDTO;
import IVS.CMS.domain.dto.response.ResPostListDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.PostCategoryRepository;
import IVS.CMS.repositories.PostRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.PostService;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.PostMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

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

        String authorName = getAuthorName(savedPost.getCreatedBy());
        return this.postMapper.postToResPostDTO(savedPost, category, authorName, null);
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

        currentPost.setMetaTitle(tempPost.getMetaTitle());
        currentPost.setMetaDescription(tempPost.getMetaDescription());
        currentPost.setCanonicalUrl(tempPost.getCanonicalUrl());
        if (tempPost.getIsIndexable() != null)
            currentPost.setIsIndexable(tempPost.getIsIndexable());
        if (tempPost.getIsFollowable() != null)
            currentPost.setIsFollowable(tempPost.getIsFollowable());
        currentPost.setOgTitle(tempPost.getOgTitle());
        currentPost.setOgDescription(tempPost.getOgDescription());
        currentPost.setOgImageId(tempPost.getOgImageId());
        currentPost.setFeaturedMediaId(tempPost.getFeaturedMediaId());

        if (tempPost.getStatus() != null) {
            currentPost.setStatus(tempPost.getStatus());
            if (tempPost.getStatus() == PostStatusEnum.PUBLISHED) {
                if (tempPost.getPublishedAt() != null) {
                    currentPost.setPublishedAt(tempPost.getPublishedAt());
                } else if (currentPost.getPublishedAt() == null) {
                    currentPost.setPublishedAt(LocalDateTime.now());
                }
            } else {
                currentPost.setPublishedAt(tempPost.getPublishedAt());
            }
        }

        currentPost.setUpdatedAt(LocalDateTime.now());
        currentPost.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

        Post updatedPost = this.postRepository.save(currentPost);

        String authorName = getAuthorName(updatedPost.getCreatedBy());
        return this.postMapper.postToResPostDTO(updatedPost, category, authorName, null);
    }

    @Override
    public ResPostDTO getPostById(long id) {
        Post post = this.postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));

        PostCategory category = null;
        if (post.getCategoryId() != null && post.getCategoryId() > 0) {
            category = this.categoryRepository.findById(post.getCategoryId()).orElse(null);
        }

        String authorName = getAuthorName(post.getCreatedBy());

        // Cần truyền URL thực tế của Media thông qua query nếu cần (tạm để null)
        String ogImageUrl = null;

        return this.postMapper.postToResPostDTO(post, category, authorName, ogImageUrl);
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
            throw new BadRequestException("Trạng thái bài viết không hợp lệ.");
        }
    }

    private String getAuthorName(Long userId) {
        if (userId == null)
            return "System";
        return this.userRepository.findById(userId).map(User::getFullName).orElse("System");
    }
}