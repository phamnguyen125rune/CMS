package IVS.CMS.services.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.OAuthLoginTicket;
import IVS.CMS.repositories.OAuthLoginTicketRepository;
import IVS.CMS.services.OAuthLoginTicketService;
import IVS.CMS.services.error.BadRequestException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthLoginTicketServiceImpl implements OAuthLoginTicketService {

    private static final int TICKET_BYTES = 32;
    private static final long TICKET_VALIDITY_SECONDS = 120;

    private final SecureRandom secureRandom = new SecureRandom();
    private final OAuthLoginTicketRepository ticketRepository;

    @Override
    @Transactional
    public String createTicket(long userId) {
        byte[] randomBytes = new byte[TICKET_BYTES];
        secureRandom.nextBytes(randomBytes);
        String rawTicket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        LocalDateTime now = LocalDateTime.now();
        OAuthLoginTicket ticket = new OAuthLoginTicket();
        ticket.setTicketHash(hash(rawTicket));
        ticket.setUserId(userId);
        ticket.setCreatedAt(now);
        ticket.setExpiredAt(now.plusSeconds(TICKET_VALIDITY_SECONDS));
        ticketRepository.save(ticket);
        return rawTicket;
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public long consumeTicket(String rawTicket) {
        if (rawTicket == null || rawTicket.isBlank()) {
            throw new BadRequestException("OAuth login code không hợp lệ");
        }

        String ticketHash = hash(rawTicket);
        OAuthLoginTicket ticket = ticketRepository.findByHashForUpdate(ticketHash)
                .orElseThrow(() -> new BadRequestException("OAuth login code không tồn tại hoặc đã được sử dụng"));

        ticketRepository.deleteByHash(ticketHash);

        if (ticket.getExpiredAt() == null || ticket.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OAuth login code đã hết hạn");
        }

        return ticket.getUserId();
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
