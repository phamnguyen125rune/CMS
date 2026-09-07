package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.services.RoleService;
import IVS.CMS.services.dto.request.role.ReqRoleDTO;
import IVS.CMS.services.dto.response.role.ResRoleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('role', 'VIEW')")
    public ResponseEntity<List<ResRoleDTO>> getAllRoles() {
        return ResponseEntity.ok(this.roleService.getAllRoles());
    }
    
    @GetMapping("/{id}/users")
    @PreAuthorize("""
        @permissionService.hasPermission('role', 'VIEW') && 
        @permissionService.hasPermission('user', 'VIEW')""")
    public ResponseEntity<List<User>> getUserRole(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.roleService.getUsersByRole(id));
    }
    
    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('role', 'CREATE')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody ReqRoleDTO role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.roleService.createRole(role));
    }

    @PutMapping
    @PreAuthorize("@permissionService.hasPermission('role', 'UPDATE')")
        public ResponseEntity<Role> updateRoleByRoleName(@Valid @RequestBody ReqRoleDTO req) {
        return ResponseEntity.ok(this.roleService.updateRoleByRoleName(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('role', 'UPDATE')")
    public ResponseEntity<Role> updateRole(@PathVariable("id") long id, @Valid @RequestBody ReqRoleDTO req) {
        return ResponseEntity.ok(this.roleService.updateRole(id, req));
    }

    @PatchMapping("/status/{id}")
    @PreAuthorize("@permissionService.hasPermission('role', 'UPDATE')")
    public ResponseEntity<Role> updateActiveRole(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.roleService.updateActiveRole(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('role', 'DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
