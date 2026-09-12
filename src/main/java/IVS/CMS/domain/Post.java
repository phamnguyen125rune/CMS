package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import IVS.CMS.domain.constants.PostStatusEnum;

@Getter
@Setter
public class Post {
    private Long postId;
    private String title;
    private String slug;
    private String summary;
    private String content;

    // Metadata (SEO)
    private String metaTitle;
    private String metaDescription;
    private String canonicalUrl;
    private Boolean isIndexable = true;
    private Boolean isFollowable = true;

    // OpenGraph & Media
    private String ogTitle;
    private String ogDescription;
    private Long ogImageId;
    private Long featuredMediaId;

    private PostStatusEnum status;
    private Long categoryId;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

}