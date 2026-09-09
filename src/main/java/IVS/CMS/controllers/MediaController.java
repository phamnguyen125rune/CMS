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

import IVS.CMS.services.dto.response.ResMediaDTO;
import IVS.CMS.services.MediaService;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    public ResponseEntity<List<ResMediaDTO>> getMedia(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "fileType", required = false) String fileType) {
        return ResponseEntity.ok(mediaService.searchAndFilter(keyword, fileType));
    }

    @PostMapping("/upload")
    public ResponseEntity<ResMediaDTO> upload(@RequestParam("file") MultipartFile file) {
        ResMediaDTO result = mediaService.upload(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{mediaId}/view")
    public ResponseEntity<Resource> viewMedia(
            @PathVariable("mediaId") long mediaId) {
        return mediaService.view(mediaId);
    }

    @GetMapping("/{mediaId}/download")
    public ResponseEntity<Resource> downloadMedia(
            @PathVariable("mediaId") long mediaId) {
        return mediaService.download(mediaId);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable("mediaId") long mediaId) {
        mediaService.delete(mediaId);
        return ResponseEntity.noContent().build();
    }
}
