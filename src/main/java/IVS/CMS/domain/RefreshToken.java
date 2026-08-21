package IVS.CMS.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshToken {
    private Long refreshTokenId;
    private Long userId;
    private String token;
    private LocalDateTime expiredAt;

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}