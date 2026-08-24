package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import IVS.CMS.services.RoleService;
import IVS.CMS.services.dto.request.role.ReqRoleDTO;
import IVS.CMS.services.dto.response.role.ResListRoleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<ResListRoleDTO>> getAllRoles() {
        return ResponseEntity.ok(this.roleService.getAllRoles());
    }
    
    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody ReqRoleDTO role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.roleService.createRole(role));
    }

    @PutMapping
        public ResponseEntity<Role> updateRoleByRoleName(@Valid @RequestBody ReqRoleDTO role) {
        return ResponseEntity.ok(this.roleService.updateRoleByRoleName(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable("id") long id, @Valid @RequestBody ReqRoleDTO role) {
        return ResponseEntity.ok(this.roleService.updateRole(id, role));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Role> updateActiveRole(@PathVariable("id") long id) {
        return ResponseEntity.ok(this.roleService.updateActiveRole(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable("id") long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}

//     @PutMapping("/{id}")
//     public ResponseEntity<Role> updateRole(@PathVariable("id") long id, @Valid @RequestBody ReqRoleDTO role) {
//         return ResponseEntity.ok(this.roleService.update(id, role));
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<Role> getRoleById(@PathVariable("id") long id) {
//         return ResponseEntity.ok(this.roleService.fetchById(id));
//     }

//     @GetMapping
//     public ResponseEntity<List<Role>> getAllRoles() {
//         return ResponseEntity.ok(this.roleService.fetchAll());
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteRole(@PathVariable("id") long id) {
//         this.roleService.delete(id);
//         return ResponseEntity.ok().build();
//     }

//     @GetMapping("/{id}/users")
//     @PreAuthorize("hasAuthority('roles:VIEW')")
//     public ResponseEntity<List<ResUserDTO>> getUsersByRole(@PathVariable("id") long id) {
//         return ResponseEntity.ok(this.roleService.fetchUsersByRoleId(id));
//     }

//     @GetMapping("/available-users")
//     @PreAuthorize("hasAuthority('roles:VIEW')")
//     public ResponseEntity<List<ResUserDTO>> getAvailableUserRoleUsers() {
//         return ResponseEntity.ok(this.roleService.fetchAvailableUserRoleUsers());
//     }

//     @PostMapping("/{roleId}/users/{userId}")
//     @PreAuthorize("hasAuthority('roles:EDIT')")
//     public ResponseEntity<ResUserDTO> addUserToRole(
//             @PathVariable("roleId") long roleId,
//             @PathVariable("userId") long userId) {
//         return ResponseEntity.ok(this.roleService.addUserToRole(roleId, userId));
//     }

//     @DeleteMapping("/{roleId}/users/{userId}")
//     @PreAuthorize("hasAuthority('roles:EDIT')")
//     public ResponseEntity<ResUserDTO> removeUserFromRole(
//             @PathVariable("roleId") long roleId,
//             @PathVariable("userId") long userId) {
//         return ResponseEntity.ok(this.roleService.removeUserFromRole(roleId, userId));
//     }
// }
