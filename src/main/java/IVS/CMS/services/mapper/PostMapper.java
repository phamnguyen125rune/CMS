package IVS.CMS.services.mapper;

import org.springframework.stereotype.Component;
import IVS.CMS.domain.PostCategory;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.dto.request.ReqPostCreateDTO;
import IVS.CMS.domain.dto.request.ReqPostUpdateDTO;
import IVS.CMS.domain.dto.response.ResPostDTO;

@Component
public class PostMapper {

    public Post reqCreateToPost(ReqPostCreateDTO dto) {
        if (dto == null) return null;
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug());
        post.setSummary(dto.getSummary());
        post.setContent(dto.getContent());
        post.setCategoryId(dto.getCategoryId());
        
        post.setMetaTitle(dto.getMetaTitle());
        post.setMetaDescription(dto.getMetaDescription());
        post.setCanonicalUrl(dto.getCanonicalUrl());
        post.setIsIndexable(dto.getIsIndexable() != null ? dto.getIsIndexable() : true);
        post.setIsFollowable(dto.getIsFollowable() != null ? dto.getIsFollowable() : true);
        
        post.setOgTitle(dto.getOgTitle());
        post.setOgDescription(dto.getOgDescription());
        post.setOgImageId(dto.getOgImageId());
        post.setFeaturedMediaId(dto.getFeaturedMediaId());
        post.setPublishedAt(dto.getPublishedAt());
        return post;
    }

    public Post reqUpdateToPost(ReqPostUpdateDTO dto) {
        if (dto == null) return null;
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug());
        post.setSummary(dto.getSummary());
        post.setCategoryId(dto.getCategoryId());
        post.setStatus(dto.getStatus());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        
        post.setMetaTitle(dto.getMetaTitle());
        post.setMetaDescription(dto.getMetaDescription());
        post.setCanonicalUrl(dto.getCanonicalUrl());
        if (dto.getIsIndexable() != null) post.setIsIndexable(dto.getIsIndexable());
        if (dto.getIsFollowable() != null) post.setIsFollowable(dto.getIsFollowable());
        
        post.setOgTitle(dto.getOgTitle());
        post.setOgDescription(dto.getOgDescription());
        post.setOgImageId(dto.getOgImageId());
        post.setFeaturedMediaId(dto.getFeaturedMediaId());
        post.setPublishedAt(dto.getPublishedAt());
        return post;
    }

    public ResPostDTO postToResPostDTO(Post post, PostCategory category, String authorName, String ogImageUrl) {
        if (post == null) return null;
        ResPostDTO res = new ResPostDTO();
        res.setId(post.getPostId());
        res.setTitle(post.getTitle());
        res.setSlug(post.getSlug());
        res.setSummary(post.getSummary());
        res.setContent(post.getContent());
        res.setStatus(post.getStatus());
        res.setPublishedAt(post.getPublishedAt());
        res.setCreatedAt(post.getCreatedAt());
        res.setUpdatedAt(post.getUpdatedAt());

        res.setAuthor(new ResPostDTO.AuthorInfo(post.getCreatedBy(), authorName != null ? authorName : "System"));

        String catName = "";
        if (category != null) {
            catName = category.getCategoryName();
            res.setCategory(new ResPostDTO.CategoryInfo(category.getCategoryId(), catName));
        } else if (post.getCategoryId() != null) {
            res.setCategory(new ResPostDTO.CategoryInfo(post.getCategoryId(), null));
        }

        // Tự động Fallback dữ liệu SEO nếu trống
        ResPostDTO.Metadata meta = new ResPostDTO.Metadata();
        String finalTitle = post.getMetaTitle() != null && !post.getMetaTitle().isBlank() ? post.getMetaTitle() : post.getTitle();
        String finalDesc = post.getMetaDescription() != null && !post.getMetaDescription().isBlank() ? post.getMetaDescription() : post.getSummary();
        
        meta.setTitle(finalTitle);
        meta.setDescription(finalDesc);
        meta.setCanonicalUrl(post.getCanonicalUrl());
        
        String robots = (Boolean.TRUE.equals(post.getIsIndexable()) ? "index" : "noindex") + ", " 
                      + (Boolean.TRUE.equals(post.getIsFollowable()) ? "follow" : "nofollow");
        meta.setRobots(robots);

        ResPostDTO.OpenGraph og = new ResPostDTO.OpenGraph();
        og.setTitle(post.getOgTitle() != null && !post.getOgTitle().isBlank() ? post.getOgTitle() : finalTitle);
        og.setDescription(post.getOgDescription() != null && !post.getOgDescription().isBlank() ? post.getOgDescription() : finalDesc);
        og.setImageUrl(ogImageUrl);
        meta.setOpenGraph(og);
        
        res.setMetadata(meta);

        // Build Structured Data cho AI
        res.setJsonLd(buildJsonLd(post, finalTitle, finalDesc, catName, res.getAuthor().getName(), ogImageUrl));

        return res;
    }

    private String buildJsonLd(Post post, String title, String desc, String categoryName, String authorName, String imageUrl) {
        String safeTitle = escapeJson(title);
        String safeDesc = escapeJson(desc);
        String safeAuthor = escapeJson(authorName);
        String safeSection = escapeJson(categoryName);
        String safeImage = escapeJson(imageUrl);
        String pubDate = post.getPublishedAt() != null ? post.getPublishedAt().toString() : (post.getCreatedAt() != null ? post.getCreatedAt().toString() : "");
        String modDate = post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : pubDate;

        return String.format("""
            {
              "@context": "https://schema.org",
              "@type": "Article",
              "headline": "%s",
              "description": "%s",
              "articleSection": "%s",
              "image": "%s",
              "author": {
                "@type": "Person",
                "name": "%s"
              },
              "datePublished": "%s",
              "dateModified": "%s"
            }
            """, safeTitle, safeDesc, safeSection, safeImage, safeAuthor, pubDate, modDate);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}