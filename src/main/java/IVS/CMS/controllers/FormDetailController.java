package IVS.CMS.controllers;

import IVS.CMS.domain.FormDetail;
import IVS.CMS.services.FormDetailService;
import IVS.CMS.services.dto.request.ReqCreateFormDetailDTO;
import IVS.CMS.services.dto.request.ReqReplyFormDetailDTO;
import IVS.CMS.services.dto.response.PaginationResponseDTO;
import IVS.CMS.services.dto.response.RestResponse;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/form-details") // Bạn có thể giữ lại "/api/v1/contacts" nếu Frontend chưa kịp đổi
public class FormDetailController {

    private final FormDetailService formDetailService;

    public FormDetailController(FormDetailService formDetailService) {
        this.formDetailService = formDetailService;
    }

    // Client endpoint: Gửi form liên hệ
    @PostMapping
    public ResponseEntity<RestResponse<FormDetail>> createFormDetail(@Valid @RequestBody ReqCreateFormDetailDTO dto) {
        FormDetail formDetail = formDetailService.createFormDetail(dto);
        RestResponse<FormDetail> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("Gửi yêu cầu tư vấn thành công");
        response.setData(formDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Admin endpoint: Lấy danh sách phân trang + search
    @GetMapping
    public ResponseEntity<RestResponse<PaginationResponseDTO>> getAllFormDetails(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PaginationResponseDTO data = formDetailService.getAllFormDetails(search, status, page, size);
        RestResponse<PaginationResponseDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy danh sách form liên hệ thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Xem chi tiết form liên hệ
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<FormDetail>> getFormDetailById(@PathVariable("id") Long id) {
        FormDetail formDetail = formDetailService.getFormDetailById(id);
        RestResponse<FormDetail> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy chi tiết form liên hệ thành công");
        response.setData(formDetail);
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Phản hồi form liên hệ
    @PostMapping("/{id}/reply")
    public ResponseEntity<RestResponse<FormDetail>> replyFormDetail(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqReplyFormDetailDTO dto
    ) {
        FormDetail formDetail = formDetailService.replyFormDetail(id, dto);
        RestResponse<FormDetail> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Gửi phản hồi thành công");
        response.setData(formDetail);
        return ResponseEntity.ok(response);
    }

    // Admin endpoint: Xóa form liên hệ
    @DeleteMapping("/{id}")
    public ResponseEntity<RestResponse<Void>> deleteFormDetail(@PathVariable("id") Long id) {
        formDetailService.deleteFormDetail(id);
        RestResponse<Void> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Xóa form liên hệ thành công");
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<RestResponse<FormDetail>> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        
        // Gọi hàm update từ Service (Đã được viết sẵn từ trước)
        FormDetail formDetail = formDetailService.updateFormDetailStatus(id, status);
        
        RestResponse<FormDetail> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Cập nhật trạng thái thành công");
        response.setData(formDetail);
        
        return ResponseEntity.ok(response);
    }
}