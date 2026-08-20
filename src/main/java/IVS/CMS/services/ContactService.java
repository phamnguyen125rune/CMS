package IVS.CMS.services;

import IVS.CMS.domain.Contact;
import IVS.CMS.domain.dto.request.ReqCreateContactDTO;
import IVS.CMS.domain.dto.request.ReqReplyContactDTO;
import IVS.CMS.domain.dto.response.PaginationResponseDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;

public interface ContactService {
    Contact createContact(ReqCreateContactDTO dto);
    Contact getContactById(Long id);
    PaginationResponseDTO getAllContacts(String search, String status, int page, int size);
    Contact replyContact(Long id, ReqReplyContactDTO dto);
    void deleteContact(Long id);
    Contact updateContactStatus(Long id, String status);
}