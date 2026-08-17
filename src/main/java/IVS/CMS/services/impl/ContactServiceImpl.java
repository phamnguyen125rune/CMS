package IVS.CMS.services.impl;

import IVS.CMS.domain.Contact;
import IVS.CMS.domain.dto.request.ReqCreateContactDTO;
import IVS.CMS.domain.dto.request.ReqReplyContactDTO;
import IVS.CMS.domain.dto.response.PaginationResponseDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.repositories.ContactRepository;
import IVS.CMS.services.ContactService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public Contact createContact(ReqCreateContactDTO dto) {
        Contact contact = new Contact();
        contact.setName(dto.getHoTen());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getSoDienThoai());
        contact.setCompany(dto.getCongTy());
        contact.setService(dto.getDichVu());
        contact.setSubject(dto.getDichVu() != null && !dto.getDichVu().isEmpty() 
            ? "Tư vấn " + dto.getDichVu() 
            : "Yêu cầu tư vấn chung");
        contact.setMessage(dto.getNoiDung());
        contact.setStatus("new");

        return contactRepository.save(contact);
    }

    @Override
    public Contact getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên hệ với ID: " + id));

        // Nếu trạng thái là 'new', chuyển thành 'read' khi mở xem
        if ("new".equalsIgnoreCase(contact.getStatus())) {
            contactRepository.updateStatus(id, "read");
            contact.setStatus("read");
        }

        return contact;
    }

   @Override
    public PaginationResponseDTO getAllContacts(String search, String status, int page, int size) {
        try {
            System.out.println("[ContactServiceImpl.getAllContacts] Called with - search: " + search + ", status: " + status + ", page: " + page + ", size: " + size);
            
            List<Contact> list = contactRepository.findAll(search, status, page, size);
            System.out.println("[ContactServiceImpl.getAllContacts] Retrieved " + (list != null ? list.size() : 0) + " contacts");
            
            long totalElements = contactRepository.count(search, status);
            System.out.println("[ContactServiceImpl.getAllContacts] Total elements: " + totalElements);
            
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // 1. Tạo và set thông tin cho đối tượng Meta
            PaginationResponseDTO.Meta meta = new PaginationResponseDTO.Meta();
            meta.setPage(page);
            meta.setPageSize(size);
            meta.setPages(totalPages);
            meta.setTotal(totalElements);

            // 2. Tạo và set thông tin cho PaginationResponseDTO
            PaginationResponseDTO response = new PaginationResponseDTO();
            response.setMeta(meta);
            response.setResult(list);

            System.out.println("[ContactServiceImpl.getAllContacts] Response prepared - meta: " + meta.getTotal() + " total");
            return response;
        } catch (Exception e) {
            System.err.println("[ContactServiceImpl.getAllContacts] ERROR: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Contact replyContact(Long id, ReqReplyContactDTO dto) {
        Contact contact = getContactById(id);
        contactRepository.updateReply(id, dto.getReplyMessage(), "replied");
        contact.setReplyMessage(dto.getReplyMessage());
        contact.setStatus("replied");
        return contact;
    }

    @Override
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên hệ với ID: " + id));
        contactRepository.deleteById(contact.getId());
    }

}
