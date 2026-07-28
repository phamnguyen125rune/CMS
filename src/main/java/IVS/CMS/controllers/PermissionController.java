package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.Permission;
import IVS.CMS.services.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('permissions:EDIT')")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody Permission permission) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.permissionService.create(permission));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:EDIT')")
    public ResponseEntity<Permission> updatePermission(@PathVariable("id") long id,
            @Valid @RequestBody Permission permission) {
        return ResponseEntity.ok(this.permissionService.update(id, permission));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:VIEW')")
    public ResponseEntity<Permission> getPermissionById(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.permissionService.fetchById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permissions:VIEW')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(this.permissionService.fetchAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permissions:EDIT')")
    public ResponseEntity<Void> deletePermission(@PathVariable("id") long id) {
        this.permissionService.delete(id);
        return ResponseEntity.ok().build();
    }
}
