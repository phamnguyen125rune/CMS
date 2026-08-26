package IVS.CMS.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.domain.Media;
import IVS.CMS.services.dto.response.ResMediaDTO;
import IVS.CMS.repositories.MediaRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.MediaService;

@Service
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    private final Path uploadPath = Paths.get("uploads");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public MediaServiceImpl(
            MediaRepository mediaRepository) {

        this.mediaRepository = mediaRepository;
    }

    @Override
    public List<ResMediaDTO> getAllMedia() {

        return mediaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResMediaDTO> search(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMedia();
        }

        return mediaRepository
                .search(keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResMediaDTO upload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File không được vượt quá 10MB");
        }

        try {

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {

                throw new IllegalArgumentException("Tên file không hợp lệ");
            }

            String extension = "";
            String fileType = "";

            int dotIndex = originalFileName.lastIndexOf(".");

            if (dotIndex >= 0 && dotIndex < originalFileName.length() - 1) {

                extension = originalFileName.substring(dotIndex).toLowerCase();

                fileType = originalFileName.substring(dotIndex + 1).toLowerCase();
            }

            String newFileName = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            String contentType = file.getContentType();

            if (contentType == null || contentType.isBlank()) {
                contentType = Files.probeContentType(filePath);
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Media media = new Media();

            media.setFileName(originalFileName);
            media.setUploadFile(newFileName);
            media.setFilePath(filePath.toString());
            media.setMimeType(contentType);
            media.setFileType(fileType);
            media.setFileSize((int) file.getSize());
            media.setUploadedBy(SecurityService.getCurrentUserId().orElse(null));
            media.setUploadedAt(LocalDateTime.now());

            Media saveMedia = mediaRepository.save(media);

            return toDTO(saveMedia);

        } catch (IOException ex) {

            throw new RuntimeException("Không thể upload file", ex);
        }
    }

    @Override
    public ResponseEntity<Resource> view(long id) {

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + id));

        try {
            Path path = Paths.get(media.getFilePath());

            if (!Files.exists(path)) {
                throw new RuntimeException("File không tồn tại: " + path);
            }

            Resource resource = new UrlResource(
                    path.toUri());

            String contentType = Files.probeContentType(path);

            if (contentType == null) {
                contentType = media.getMimeType();
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + media.getFileName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Không thể đọc file: " + media.getFilePath(),
                    e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(long id) {

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ảnh " + id + " không tồn tại"));

        try {

            Path filePath = uploadPath.resolve(media.getUploadFile()).normalize();

            if (!Files.exists(filePath)) {

                throw new RuntimeException("File không tồn tại: " + filePath.toAbsolutePath());
            }

            Resource resource = new FileSystemResource(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + media.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(media.getMimeType()))
                    .contentLength(Files.size(filePath))
                    .body(resource);

        } catch (IOException e) {

            throw new RuntimeException("Không thể tải ảnh", e);
        }
    }

    @Override
    public void delete(long id) {

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ảnh " + id + " không tồn tại"));

        try {

            if (media.getUploadFile() != null) {

                Path filePath = uploadPath.resolve(media.getUploadFile());
                Files.deleteIfExists(filePath);
            }

            mediaRepository.delete(media);

        } catch (IOException e) {
            throw new RuntimeException("Không thể xóa file", e);
        }
    }

    private ResMediaDTO toDTO(Media media) {

        ResMediaDTO response = new ResMediaDTO();

        response.setMediaId(media.getMediaId());
        response.setFileName(media.getFileName());
        response.setFilePath("/api/v1/media/" + media.getMediaId() + "/view");
        response.setMimeType(media.getMimeType());
        response.setFileSize(media.getFileSize());
        response.setUploadedBy(SecurityService.getCurrentUserId().orElse(null));
        response.setUploadedAt(media.getUploadedAt());

        return response;
    }
}