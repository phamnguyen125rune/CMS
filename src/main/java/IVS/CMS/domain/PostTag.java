package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostTag {

    private Long postTagId;

    private Long postId;
    private Long tagId;
}