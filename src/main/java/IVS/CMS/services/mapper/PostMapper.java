package IVS.CMS.services.mapper;

import org.springframework.stereotype.Component;
import IVS.CMS.domain.Category;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.dto.request.ReqPostCreateDTO;
import IVS.CMS.domain.dto.request.ReqPostUpdateDTO;
import IVS.CMS.domain.dto.response.ResPostDTO;
import IVS.CMS.domain.dto.response.ResPostListDTO;

@Component
public class PostMapper {

    public Post reqCreateToPost(ReqPostCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug());
        post.setSummary(dto.getSummary());
        post.setCategoryId(dto.getCategoryId());
        post.setContent(dto.getContent());

        return post;
    }

    public Post reqUpdateToPost(ReqPostUpdateDTO dto) {
        if (dto == null) {
            return null;
        }
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug());
        post.setSummary(dto.getSummary());
        post.setCategoryId(dto.getCategoryId());
        post.setStatus(dto.getStatus());

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }

        return post;
    }

    public ResPostDTO postToResPostDTO(Post post, Category category) {
        if (post == null) {
            return null;
        }
        ResPostDTO res = new ResPostDTO();
        res.setPostId(post.getPostId());
        res.setTitle(post.getTitle());
        res.setSlug(post.getSlug());
        res.setSummary(post.getSummary());
        res.setStatus(post.getStatus());
        res.setCreatedAt(post.getCreatedAt());
        res.setUpdatedAt(post.getUpdatedAt());
        res.setContent(post.getContent());

        if (post.getCreatedBy() != null) {
            res.setCreatedBy(new ResPostDTO.UserPost(post.getCreatedBy(), null));
        }
        if (post.getUpdatedBy() != null) {
            res.setUpdatedBy(new ResPostDTO.UserPost(post.getUpdatedBy(), null));
        }

        if (category != null) {
            res.setCategory(new ResPostDTO.CategoryPost(category.getCategoryId(), category.getCategoryName()));
        } else if (post.getCategoryId() != null) {
            res.setCategory(new ResPostDTO.CategoryPost(post.getCategoryId(), null));
        }
        return res;
    }

    public ResPostListDTO postToResPostListDTO(Post post, Category category) {
        if (post == null) {
            return null;
        }
        ResPostListDTO res = new ResPostListDTO();
        res.setPostId(post.getPostId());
        res.setTitle(post.getTitle());
        res.setSlug(post.getSlug());
        res.setSummary(post.getSummary());
        res.setStatus(post.getStatus());
        res.setCreatedAt(post.getCreatedAt());
        res.setUpdatedAt(post.getUpdatedAt());
        if (post.getCreatedBy() != null) {
            res.setCreatedBy(new ResPostListDTO.UserPost(post.getCreatedBy(), null));
        }
        if (post.getUpdatedBy() != null) {
            res.setUpdatedBy(new ResPostListDTO.UserPost(post.getUpdatedBy(), null));
        }

        if (category != null) {
            res.setCategory(new ResPostListDTO.CategoryPost(category.getCategoryId(), category.getCategoryName()));
        } else if (post.getCategoryId() != null) {
            res.setCategory(new ResPostListDTO.CategoryPost(post.getCategoryId(), null));
        }
        return res;
    }
}