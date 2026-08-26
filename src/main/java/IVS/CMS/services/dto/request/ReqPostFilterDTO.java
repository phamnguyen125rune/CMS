package IVS.CMS.services.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ReqPostFilterDTO {
    private String keyword;
    private String status;
    private Long categoryId;
    private Long authorId;
    private LocalDate fromDate;
    private LocalDate toDate;
}