package IVS.CMS.repositories;

import java.util.Optional;
import IVS.CMS.domain.RefreshToken;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenAndEmail(String token, String email);

    void deleteByUserId(long userId);
}