package IVS.CMS.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.domain.dto.response.ResMediaDTO;
import IVS.CMS.services.MediaService;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    public ResponseEntity<List<ResMediaDTO>> getMedia(@RequestParam(required = false) String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(mediaService.getAllMedia());
        }
        return ResponseEntity.ok(mediaService.search(keyword));
    }

    @PostMapping("/upload")
    public ResponseEntity<ResMediaDTO> upload(@RequestParam("file") MultipartFile file) {
        ResMediaDTO result = mediaService.upload(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewMedia(
            @PathVariable long id) {
        return mediaService.view(id);
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMedia(
            @PathVariable long id) {
        return mediaService.download(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable long id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
