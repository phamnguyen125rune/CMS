package IVS.CMS.controllers;

import IVS.CMS.security.SecurityService;
import IVS.CMS.services.BannerService;
import IVS.CMS.services.dto.request.ReqCreateBannerDTO;
import IVS.CMS.services.dto.request.ReqUpdateBannerDTO;
import IVS.CMS.services.dto.response.ResBannerDTO;
import IVS.CMS.services.dto.response.RestResponse;
import IVS.CMS.services.dto.response.ResultPaginationDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Public API cho Trang chủ lấy danh sách banner đang hoạt động.
     */
    @GetMapping("/public")
    public ResponseEntity<RestResponse<List<ResBannerDTO>>> getPublicBanners(
            @RequestParam(value = "position", required = false, defaultValue = "HOME_HERO") String position) {
        List<ResBannerDTO> data = bannerService.getActiveBanners(position);
        if ((data == null || data.isEmpty()) && position != null && !position.trim().isEmpty() && !"ALL".equalsIgnoreCase(position.trim())) {
            data = bannerService.getActiveBanners(null);
        }
        RestResponse<List<ResBannerDTO>> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy danh sách banner thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Lấy danh sách banner có phân trang & tìm kiếm.
     */
    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('banner', 'VIEW')")
    public ResponseEntity<RestResponse<ResultPaginationDTO>> getAllBanners(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        ResultPaginationDTO data = bannerService.getAllBanners(search, position, isActive, page, size);
        RestResponse<ResultPaginationDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy danh sách banner thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Lấy chi tiết 1 banner theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('banner', 'VIEW')")
    public ResponseEntity<RestResponse<ResBannerDTO>> getBannerById(@PathVariable("id") Long id) {
        ResBannerDTO data = bannerService.getBannerById(id);
        if (data == null) {
            RestResponse<ResBannerDTO> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
            response.setError("Không tìm thấy banner với ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        RestResponse<ResBannerDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Lấy thông tin banner thành công");
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Tạo banner mới.
     */
    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('banner', 'CREATE')")
    public ResponseEntity<RestResponse<ResBannerDTO>> createBanner(
            @Valid @RequestBody ReqCreateBannerDTO dto) {
        Long currentUserId = SecurityService.getCurrentUserId().orElse(1L);
        ResBannerDTO created = bannerService.createBanner(dto, currentUserId);
        RestResponse<ResBannerDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("Tạo mới banner thành công");
        response.setData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin: Cập nhật banner.
     */
    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('banner', 'UPDATE')")
    public ResponseEntity<RestResponse<ResBannerDTO>> updateBanner(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqUpdateBannerDTO dto) {
        Long currentUserId = SecurityService.getCurrentUserId().orElse(1L);
        ResBannerDTO updated = bannerService.updateBanner(id, dto, currentUserId);
        if (updated == null) {
            RestResponse<ResBannerDTO> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
            response.setError("Không tìm thấy banner với ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        RestResponse<ResBannerDTO> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Cập nhật banner thành công");
        response.setData(updated);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Bật/tắt trạng thái banner nhanh.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("@permissionService.hasPermission('banner', 'UPDATE')")
    public ResponseEntity<RestResponse<Boolean>> toggleStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> body) {
        Boolean isActive = body.getOrDefault("isActive", true);
        boolean success = bannerService.toggleStatus(id, isActive);
        RestResponse<Boolean> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Cập nhật trạng thái banner thành công");
        response.setData(success);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Xóa banner.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('banner', 'DELETE')")
    public ResponseEntity<RestResponse<Boolean>> deleteBanner(@PathVariable("id") Long id) {
        boolean success = bannerService.deleteBanner(id);
        RestResponse<Boolean> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Xóa banner thành công");
        response.setData(success);
        return ResponseEntity.ok(response);
    }
}
