package IVS.CMS.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import IVS.CMS.domain.dto.request.ReqChangePasswordDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqUserUpdateDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;
import IVS.CMS.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ResUserCreateDTO> create(@Valid @RequestBody ReqUserCreateDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResUserDTO> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqUserUpdateDTO req) {
        return ResponseEntity.ok(this.userService.updateUser(id, req));
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(this.userService.findAll(page, pageSize));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> body) {
        this.userService.updateStatus(id, body.get("isActive"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ReqChangePasswordDTO req) {
        this.userService.changePassword(req);
        return ResponseEntity.ok().build();
    }
}