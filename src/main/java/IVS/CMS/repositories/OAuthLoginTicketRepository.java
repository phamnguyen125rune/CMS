package IVS.CMS.repositories;

import java.util.Optional;

import IVS.CMS.domain.OAuthLoginTicket;

public interface OAuthLoginTicketRepository {
    void save(OAuthLoginTicket ticket);

    Optional<OAuthLoginTicket> findByHashForUpdate(String ticketHash);

    void deleteByHash(String ticketHash);
}
