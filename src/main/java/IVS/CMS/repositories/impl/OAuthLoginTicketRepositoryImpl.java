package IVS.CMS.repositories.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.OAuthLoginTicket;
import IVS.CMS.repositories.OAuthLoginTicketRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OAuthLoginTicketRepositoryImpl implements OAuthLoginTicketRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void save(OAuthLoginTicket ticket) {
        String sql = """
                INSERT INTO oauth_login_tickets (ticket_hash, user_id, expired_at, created_at)
                VALUES (:ticketHash, :userId, :expiredAt, :createdAt)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ticketHash", ticket.getTicketHash())
                .addValue("userId", ticket.getUserId())
                .addValue("expiredAt", ticket.getExpiredAt())
                .addValue("createdAt", ticket.getCreatedAt());
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<OAuthLoginTicket> findByHashForUpdate(String ticketHash) {
        String sql = """
                SELECT ticket_hash, user_id, expired_at, created_at
                FROM oauth_login_tickets
                WHERE ticket_hash = :ticketHash
                FOR UPDATE
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("ticketHash", ticketHash),
                (rs, rowNum) -> {
                    OAuthLoginTicket ticket = new OAuthLoginTicket();
                    ticket.setTicketHash(rs.getString("ticket_hash"));
                    ticket.setUserId(rs.getLong("user_id"));
                    ticket.setExpiredAt(rs.getObject("expired_at", LocalDateTime.class));
                    ticket.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                    return ticket;
                })
                .stream()
                .findFirst();
    }

    @Override
    public void deleteByHash(String ticketHash) {
        jdbcTemplate.update(
                "DELETE FROM oauth_login_tickets WHERE ticket_hash = :ticketHash",
                new MapSqlParameterSource("ticketHash", ticketHash));
    }
}
