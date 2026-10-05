package IVS.CMS.services;

public interface OAuthLoginTicketService {
    String createTicket(long userId);

    long consumeTicket(String rawTicket);
}
