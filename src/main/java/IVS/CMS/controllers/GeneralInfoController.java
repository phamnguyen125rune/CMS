package IVS.CMS.controllers;

import IVS.CMS.domain.GeneralInfo;
import IVS.CMS.services.GeneralInfoService;
import IVS.CMS.services.dto.request.ReqUpdateGeneralInfoDTO;
import IVS.CMS.services.dto.response.RestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/general-info")
public class GeneralInfoController {

    private final GeneralInfoService service;

    public GeneralInfoController(GeneralInfoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<RestResponse<GeneralInfo>> getGeneralInfo() {
        GeneralInfo data = service.getGeneralInfo();
        RestResponse<GeneralInfo> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy thông tin chung thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<RestResponse<GeneralInfo>> updateGeneralInfo(@Valid @RequestBody ReqUpdateGeneralInfoDTO dto) {
        // Tạm gán userId là 1L, thực tế lấy từ Token bảo mật
        GeneralInfo updated = service.saveOrUpdateGeneralInfo(dto, 1L);
        RestResponse<GeneralInfo> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Cập nhật thông tin chung thành công");
        response.setData(updated);
        return ResponseEntity.ok(response);
    }
}