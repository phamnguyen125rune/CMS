package IVS.CMS.controllers;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.domain.dto.request.ReqUpdateProfileDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqUserUpdateDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<ResUserCreateDTO> create(@Valid @RequestBody ReqUserCreateDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.createUser(user));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResUserDTO> getMyProfile() {
        return ResponseEntity.ok(this.userService.getMyProfile());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResUserDTO> updateMyProfile(@Valid @RequestBody ReqUpdateProfileDTO req) {
        return ResponseEntity.ok(this.userService.updateMyProfile(req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:VIEW')")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<ResUserDTO> updateUser(@PathVariable("id") Long id,
            @Valid @RequestBody ReqUserUpdateDTO req) {
        return ResponseEntity.ok(this.userService.updateUser(id, req));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('users:VIEW')")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(value = "page", defaultValue = "1") int pages,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(this.userService.findAll(pages, pageSize));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<Void> softDeleteUser(@PathVariable("id") Long id) {
        this.userService.softDeleteUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<Void> hardDeleteUser(@PathVariable("id") Long id) {
        this.userService.hardDeleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<Void> restoreUser(@PathVariable("id") Long id) {
        this.userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('users:VIEW')")
    public ResponseEntity<List<ResUserDTO>> getDeletedUsers() {
        return ResponseEntity.ok(this.userService.getDeletedUsers());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<Void> toggleStatus(@PathVariable("id") long id, @RequestBody Map<String, Boolean> body) {
        this.userService.toggleUserStatus(id, body.get("isActive"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('users:EDIT')")
    public ResponseEntity<Void> resetPassword(@PathVariable("id") long id) {
        this.userService.resetUserPassword(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/avatar")
    @PreAuthorize("hasAuthority('profile:EDIT')")
    public ResponseEntity<Map<String, String>> uploadMyAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = this.userService.uploadMyAvatar(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("avatarUrl", avatarUrl));
    }
}
