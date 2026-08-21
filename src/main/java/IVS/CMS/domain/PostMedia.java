package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMedia {
    private Long postId;
    private Long mediaId;
    private Integer displayOrder;
}