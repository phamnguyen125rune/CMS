package IVS.CMS.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import IVS.CMS.domain.constants.PostStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResPostDTO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private PostStatusEnum status;
    private CategoryInfo category;
    private AuthorInfo author;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Metadata metadata;
    private Object jsonLd;

    private List<TagInfo> tags;
    private List<MediaInfo> mediaList;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagInfo {
        private Long id;
        private String name;
        private String slug;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MediaInfo {
        private Long id;
        private String filePath;
        private String fileType;
        private String altText;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class Metadata {
        private String title;
        private String description;
        private String canonicalUrl;
        private String robots;
        private OpenGraph openGraph;
    }

    @Getter
    @Setter
    public static class OpenGraph {
        private String title;
        private String description;
        private String imageUrl;
    }
}