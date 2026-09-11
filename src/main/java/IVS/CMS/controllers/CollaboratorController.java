package IVS.CMS.controllers;

import IVS.CMS.services.CollaboratorService;
import IVS.CMS.services.dto.request.ReqCollaborator;
import IVS.CMS.services.dto.response.ResCollaborator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collaborator")
@RequiredArgsConstructor
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    @GetMapping
    public ResponseEntity<List<ResCollaborator>> getCollaborator() {
        return ResponseEntity.ok(collaboratorService.getCollaborator());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResCollaborator> findById(@PathVariable Long id) {
        return ResponseEntity.ok(collaboratorService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResCollaborator> create(
            @RequestBody ReqCollaborator request) {
        return ResponseEntity.ok(collaboratorService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResCollaborator> update(
            @PathVariable Long id,
            @RequestBody ReqCollaborator request) {
        return ResponseEntity.ok(collaboratorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        collaboratorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}