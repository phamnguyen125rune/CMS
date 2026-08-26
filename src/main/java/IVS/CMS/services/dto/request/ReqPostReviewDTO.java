package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import IVS.CMS.domain.constants.PostReviewActionEnum;

@Getter
@Setter
public class ReqPostReviewDTO {
    @NotNull(message = "Hành động duyệt không được để trống")
    private PostReviewActionEnum action;

    private String comment;
}