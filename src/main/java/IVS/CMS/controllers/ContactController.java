package IVS.CMS.controllers;


import IVS.CMS.domain.Contact;
import IVS.CMS.domain.dto.request.ReqCreateContactDTO;
import IVS.CMS.domain.dto.request.ReqReplyContactDTO;
import IVS.CMS.domain.dto.response.PaginationResponseDTO;
import IVS.CMS.domain.dto.response.RestResponse;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.services.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // Client endpoint: Gửi form liên hệ
    @PostMapping
    public ResponseEntity<RestResponse<Contact>> createContact(@Valid @RequestBody ReqCreateContactDTO dto) {
        Contact contact = contactService.createContact(dto);
        RestResponse<Contact> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("Gửi yêu cầu tư vấn thành công");
        response.setData(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Admin endpoint: Lấy danh sách phân trang + search
    @GetMapping
    public ResponseEntity<RestResponse<PaginationResponseDTO>> getAllContacts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PaginationResponseDTO data = contactService.getAllContacts(search, status, page, size);
        RestResponse<PaginationResponseDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy danh sách liên hệ thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Xem chi tiết liên hệ
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<Contact>> getContactById(@PathVariable("id") Long id) {
        Contact contact = contactService.getContactById(id);
        RestResponse<Contact> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy chi tiết liên hệ thành công");
        response.setData(contact);
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Phản hồi liên hệ
    @PostMapping("/{id}/reply")
    public ResponseEntity<RestResponse<Contact>> replyContact(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqReplyContactDTO dto
    ) {
        Contact contact = contactService.replyContact(id, dto);
        RestResponse<Contact> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Gửi phản hồi thành công");
        response.setData(contact);
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Xóa liên hệ
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteContact(@PathVariable("id") Long id) {
        contactService.deleteContact(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Xóa liên hệ thành công");
        return ResponseEntity.ok(response);
    }

}
