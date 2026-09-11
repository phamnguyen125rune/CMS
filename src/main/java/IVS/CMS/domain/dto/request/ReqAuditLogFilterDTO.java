package IVS.CMS.domain.dto.request;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAuditLogFilterDTO {

    private String keyword;
    private String preset = "7d";

    // from < to <= now
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String entityType;
    private Integer entityId;

    private String action;
    private List<String> actions;

    private Long userId;

    // TODO: Sẽ được chỉnh thành chỉ tìm hành vi log của Admin trong tương lai khi định chuẩn lại seed data.
    private Boolean anonymousOnly = false;

    private Integer statusCode;
    private Integer minStatusCode;
    private String statusGroup;

    private String sort = "createdAt,desc";

    private Integer page = 1;
    private Integer size = 20;

    public LocalDateTime resolveTo() {
        if (to != null) {
            return to;
        }
        if (toDate != null) {
            return toDate.atTime(23, 59, 59, 999999000);
        }
        return LocalDateTime.now();
    }

    public LocalDateTime resolveFrom() {
        LocalDateTime resolvedTo = resolveTo();
        if (from != null) {
            return from;
        }
        if (fromDate != null) {
            return fromDate.atStartOfDay();
        }

        String p = (preset != null && !preset.isBlank()) ? preset.trim().toLowerCase() : "7d";
        return switch (p) {
            case "1h" -> resolvedTo.minusHours(1);
            case "24h" -> resolvedTo.minusHours(24);
            case "30d" -> resolvedTo.minusDays(30);
            case "7d" -> resolvedTo.minusDays(7);
            default -> resolvedTo.minusDays(7);
        };
    }

    public List<String> resolveActions() {
        List<String> list = new ArrayList<>();
        if (actions != null && !actions.isEmpty()) {
            for (String item : actions) {
                if (item != null && !item.isBlank()) {
                    for (String part : item.split(",")) {
                        String clean = part.trim();
                        if (!clean.isEmpty() && !list.contains(clean)) {
                            list.add(clean);
                        }
                    }
                }
            }
        }
        if (action != null && !action.isBlank()) {
            String act = action.trim();
            if (!list.contains(act)) {
                list.add(act);
            }
        }
        return list;
    }
}
