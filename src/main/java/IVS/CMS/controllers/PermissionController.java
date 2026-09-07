package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.services.PermissionService;
import IVS.CMS.services.dto.request.role.ReqPermissionApiLinkDTO;
import IVS.CMS.services.dto.request.role.ReqPermissionIdDTO;
import IVS.CMS.services.dto.response.role.ResApiActionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionService permissionService;

    @GetMapping("/apiAction")
    @PreAuthorize("@permissionService.hasPermission('permission', 'VIEW')")
    public ResponseEntity<List<ResApiActionDTO>> getAllActions() {
        return ResponseEntity.ok(this.permissionService.getAllApiActions());
    }

    @PutMapping("/update/{roleId}")
    @PreAuthorize("@permissionService.hasPermission('permission', 'UPDATE')")
    public ResponseEntity<String> updateRolePermissionsById(@PathVariable("roleId") long roleId, @Valid @RequestBody ReqPermissionIdDTO req) {
        return ResponseEntity.ok(
                permissionService.assignPermissionToRoleById(roleId, req)
        );
    }

    @PutMapping("/update/link/{roleId}")
    @PreAuthorize("@permissionService.hasPermission('permission', 'UPDATE')")
    public ResponseEntity<String> updateRolePermissionsByApiLink(@PathVariable("roleId") long roleId, @Valid @RequestBody ReqPermissionApiLinkDTO req) {
        return ResponseEntity.ok(
                permissionService.assignPermissionToRoleByApiLink(roleId, req)
        );
    }
}
