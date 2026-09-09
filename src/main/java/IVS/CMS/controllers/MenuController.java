package IVS.CMS.controllers;

import IVS.CMS.services.MenuService;
import IVS.CMS.services.dto.request.ReqMenuDTO;
import IVS.CMS.services.dto.response.ResMenuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<ResMenuDTO>> getMenu() {
        return ResponseEntity.ok(menuService.getMenu());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResMenuDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResMenuDTO> create(@RequestBody ReqMenuDTO request) {
        return ResponseEntity.ok(menuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResMenuDTO> update(@PathVariable Long id, @RequestBody ReqMenuDTO request) {
        return ResponseEntity.ok(menuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}