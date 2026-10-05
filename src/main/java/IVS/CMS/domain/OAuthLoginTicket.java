package IVS.CMS.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLoginTicket {
    private String ticketHash;
    private Long userId;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
}
