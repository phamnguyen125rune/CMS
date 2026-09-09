package IVS.CMS.domain.dto.request;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAuditLogFilterDTO {
    private String keyword;
    private String entityType;
    private String action;
    private Long userId;
    private Integer statusCode;
    private LocalDate fromDate;
    private LocalDate toDate;
}
