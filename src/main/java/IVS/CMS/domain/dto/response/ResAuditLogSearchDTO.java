package IVS.CMS.domain.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResAuditLogSearchDTO {

    private SummaryStats summary;
    private ResultPaginationDTO.Meta meta;
    @Builder.Default
    private List<ResAuditLogDTO> items = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryStats {
        private long totalRecords;
        private long executionTimeMs;
        private HealthRatio healthRatio;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthRatio {
        private long success2xx;
        private long clientError4xx;
        private long serverError5xx;
        private long other;
        private double errorRatePercent;
    }
}
