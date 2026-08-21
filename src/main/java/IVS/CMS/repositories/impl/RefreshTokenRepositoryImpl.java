package IVS.CMS.repositories.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.RefreshToken;
import IVS.CMS.repositories.RefreshTokenRepository;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RefreshTokenRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RefreshToken> rowMapper = new RowMapper<RefreshToken>() {
        @Override
        public RefreshToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefreshToken token = new RefreshToken();
            token.setRefreshTokenId(rs.getLong("refresh_token_id"));
            token.setUserId(rs.getLong("user_id"));
            token.setToken(rs.getString("token"));
            if (rs.getTimestamp("expired_at") != null) {
                token.setExpiredAt(rs.getTimestamp("expired_at").toLocalDateTime());
            }
            return token;
        }
    };

    @Override
    public void save(RefreshToken refreshToken) {
        String sql = """
                    INSERT INTO refresh_tokens (user_id, token, expired_at, created_at)
                    VALUES (:userId, :token, :expiredAt, NOW(6))
                """; // Script SQL đang để ID không auto_increment, giả định DB sẽ tự gen hoặc ta
                     // phải thiết lập Auto Increment cho nó.

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", refreshToken.getUserId())
                .addValue("token", refreshToken.getToken())
                .addValue("expiredAt", refreshToken.getExpiredAt());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<RefreshToken> findByTokenAndEmail(String token, String email) {
        String sql = """
                    SELECT rt.*
                    FROM refresh_tokens rt
                    INNER JOIN users u ON rt.user_id = u.user_id
                    WHERE rt.token = :token
                      AND u.email = :email
                      AND u.deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", token)
                .addValue("email", email);

        return jdbcTemplate.query(sql, params, rowMapper).stream().findFirst();
    }

    @Override
    public void deleteByUserId(long userId) {
        String sql = "DELETE FROM refresh_tokens WHERE user_id = :userId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("userId", userId));
    }
}