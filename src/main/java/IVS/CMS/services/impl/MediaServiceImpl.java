package IVS.CMS.services.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.domain.Media;
import IVS.CMS.repositories.MediaRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.MediaService;
import IVS.CMS.services.dto.request.ReqMediaResize;
import IVS.CMS.services.dto.response.ResMediaDTO;
import IVS.CMS.services.error.BadRequestException;

@Service
public class MediaServiceImpl implements MediaService {

        private final MediaRepository mediaRepository;

        @Value("${media.upload-dir}")
        private String mediaUploadDir;

        private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

        public MediaServiceImpl(MediaRepository mediaRepository) {
                this.mediaRepository = mediaRepository;
        }

        private Path getUploadPath() {
                return Paths.get(mediaUploadDir).toAbsolutePath().normalize();
        }

        @Override
        public List<ResMediaDTO> getAllMedia() {
                return mediaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
        }

        @Override
        public List<ResMediaDTO> searchAndFilter(String keyword, String fileType) {
                return mediaRepository.searchAndFilter(keyword, fileType).stream().map(this::toDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public ResMediaDTO upload(MultipartFile file) {
                if (file == null || file.isEmpty())
                        throw new IllegalArgumentException("File không được để trống");
                if (file.getSize() > MAX_FILE_SIZE)
                        throw new IllegalArgumentException("File không được vượt quá 10MB");

                try {
                        Path uploadPath = getUploadPath();
                        if (!Files.exists(uploadPath))
                                Files.createDirectories(uploadPath);

                        String originalFileName = file.getOriginalFilename();
                        if (originalFileName == null || originalFileName.isBlank())
                                throw new IllegalArgumentException("Tên file không hợp lệ");

                        String extension = "";
                        String fileType = "";
                        int dotIndex = originalFileName.lastIndexOf(".");

                        if (dotIndex >= 0 && dotIndex < originalFileName.length() - 1) {
                                extension = originalFileName.substring(dotIndex).toLowerCase();
                                fileType = originalFileName.substring(dotIndex + 1).toLowerCase();
                        }

                        String newFileName = UUID.randomUUID() + extension;
                        Path filePath = uploadPath.resolve(newFileName);

                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        String contentType = file.getContentType();
                        if (contentType == null || contentType.isBlank())
                                contentType = Files.probeContentType(filePath);
                        if (contentType == null)
                                contentType = "application/octet-stream";

                        Integer mediaWidth = null;
                        Integer mediaHeight = null;

                        if (contentType.startsWith("image/")) {
                                BufferedImage image = ImageIO.read(filePath.toFile());
                                if (image != null) {
                                        mediaWidth = image.getWidth();
                                        mediaHeight = image.getHeight();
                                }
                        }

                        Media media = new Media();
                        media.setFileName(originalFileName);
                        media.setUploadFile(newFileName);
                        media.setFilePath("uploads/" + newFileName);
                        media.setMimeType(contentType);
                        media.setFileType(fileType);
                        media.setFileSize((int) file.getSize());
                        media.setMediaWidth(mediaWidth);
                        media.setMediaHeight(mediaHeight);
                        SecurityService.getCurrentUserId().ifPresent(media::setUploadedBy);
                        media.setUploadedAt(LocalDateTime.now());

                        return toDTO(mediaRepository.save(media));

                } catch (IOException ex) {
                        throw new RuntimeException("Không thể upload file", ex);
                }
        }

        @Override
        public ResMediaDTO resize(long id, ReqMediaResize request) {
                if (request == null)
                        throw new IllegalArgumentException("Thông tin resize không được để trống");
                if (request.getWidth() == null || request.getHeight() == null)
                        throw new IllegalArgumentException("Chiều rộng và chiều cao không được để trống");
                if (request.getWidth() <= 0 || request.getHeight() <= 0)
                        throw new IllegalArgumentException("Chiều rộng và chiều cao phải lớn hơn 0");

                Media sourceMedia = mediaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh với id: " + id));

                if (sourceMedia.getMimeType() == null || !sourceMedia.getMimeType().startsWith("image/")) {
                        throw new IllegalArgumentException("Chỉ có thể resize file ảnh");
                }

                try {
                        Path uploadPath = getUploadPath();
                        if (!Files.exists(uploadPath))
                                Files.createDirectories(uploadPath);

                        Path sourcePath = uploadPath.resolve(sourceMedia.getUploadFile()).normalize();

                        if (!Files.exists(sourcePath))
                                throw new RuntimeException("File ảnh không tồn tại: " + sourcePath.toAbsolutePath());

                        BufferedImage sourceImage = ImageIO.read(sourcePath.toFile());
                        if (sourceImage == null)
                                throw new RuntimeException("Không thể đọc file ảnh");

                        int width = request.getWidth();
                        int height = request.getHeight();
                        int imageType = sourceImage.getType();

                        if (imageType == BufferedImage.TYPE_CUSTOM || imageType == 0) {
                                imageType = sourceMedia.getMimeType().equalsIgnoreCase("image/png")
                                                ? BufferedImage.TYPE_INT_ARGB
                                                : BufferedImage.TYPE_INT_RGB;
                        }

                        BufferedImage resizedImage = new BufferedImage(width, height, imageType);
                        Graphics2D graphics = resizedImage.createGraphics();

                        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        graphics.drawImage(sourceImage, 0, 0, width, height, null);
                        graphics.dispose();

                        String extension = getImageExtension(sourceMedia);

                        if (!isSupportedImageFormat(extension)) {
                                throw new IllegalArgumentException("Định dạng ảnh không hỗ trợ resize: " + extension);
                        }

                        String newFileName = UUID.randomUUID() + "." + extension;
                        Path newFilePath = uploadPath.resolve(newFileName);

                        boolean written = ImageIO.write(resizedImage, extension, newFilePath.toFile());

                        if (!written)
                                throw new RuntimeException("Không thể tạo file ảnh mới");

                        long newFileSize = Files.size(newFilePath);

                        if (newFileSize > MAX_FILE_SIZE) {
                                Files.deleteIfExists(newFilePath);
                                throw new IllegalArgumentException("File ảnh sau khi resize vượt quá 10MB");
                        }

                        Media resizedMedia = new Media();
                        resizedMedia.setFileName(sourceMedia.getFileName());
                        resizedMedia.setUploadFile(newFileName);
                        resizedMedia.setFilePath("uploads/" + newFileName);
                        resizedMedia.setMimeType(sourceMedia.getMimeType());
                        resizedMedia.setFileType(extension);
                        resizedMedia.setFileSize((int) newFileSize);
                        resizedMedia.setMediaWidth(width);
                        resizedMedia.setMediaHeight(height);
                        SecurityService.getCurrentUserId().ifPresent(resizedMedia::setUploadedBy);
                        resizedMedia.setUploadedAt(LocalDateTime.now());

                        try {
                                return toDTO(mediaRepository.save(resizedMedia));
                        } catch (Exception ex) {
                                Files.deleteIfExists(newFilePath);
                                throw ex;
                        }

                } catch (IOException ex) {
                        throw new RuntimeException("Không thể resize ảnh", ex);
                }
        }

        private String getImageExtension(Media media) {
                String fileType = media.getFileType();

                if (fileType != null && !fileType.isBlank()) {
                        if ("jpeg".equalsIgnoreCase(fileType))
                                return "jpg";
                        return fileType.toLowerCase();
                }

                String fileName = media.getUploadFile();

                if (fileName != null) {
                        int dotIndex = fileName.lastIndexOf(".");
                        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
                                String extension = fileName.substring(dotIndex + 1).toLowerCase();
                                if ("jpeg".equals(extension))
                                        return "jpg";
                                return extension;
                        }
                }

                return "jpg";
        }

        private boolean isSupportedImageFormat(String extension) {
                return "jpg".equalsIgnoreCase(extension)
                                || "png".equalsIgnoreCase(extension)
                                || "gif".equalsIgnoreCase(extension)
                                || "bmp".equalsIgnoreCase(extension);
        }

        @Override
        public ResponseEntity<Resource> view(long id) {
                Media media = mediaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + id));

                try {
                        Path uploadPath = getUploadPath();
                        Path filePath = uploadPath.resolve(media.getUploadFile()).normalize();

                        if (!Files.exists(filePath))
                                throw new RuntimeException("File không tồn tại: " + filePath.toAbsolutePath());

                        Resource resource = new FileSystemResource(filePath);
                        String contentType = media.getMimeType();

                        if (contentType == null || contentType.isBlank())
                                contentType = Files.probeContentType(filePath);
                        if (contentType == null)
                                contentType = "application/octet-stream";

                        return ResponseEntity.ok()
                                        .contentType(MediaType.parseMediaType(contentType))
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "inline; filename=\"" + media.getFileName() + "\"")
                                        .contentLength(Files.size(filePath))
                                        .body(resource);

                } catch (IOException e) {
                        throw new RuntimeException("Không thể đọc file: " + media.getFileName(), e);
                }
        }

        @Override
        public ResponseEntity<Resource> download(long id) {
                Media media = mediaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("File " + id + " không tồn tại"));

                try {
                        Path uploadPath = getUploadPath();
                        Path filePath = uploadPath.resolve(media.getUploadFile()).normalize();

                        if (!Files.exists(filePath))
                                throw new RuntimeException("File không tồn tại: " + filePath.toAbsolutePath());

                        Resource resource = new FileSystemResource(filePath);
                        String contentType = media.getMimeType();

                        if (contentType == null || contentType.isBlank())
                                contentType = "application/octet-stream";

                        return ResponseEntity.ok()
                                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                                        "attachment; filename=\"" + media.getFileName() + "\"")
                                        .contentType(MediaType.parseMediaType(contentType))
                                        .contentLength(Files.size(filePath))
                                        .body(resource);

                } catch (IOException e) {
                        throw new RuntimeException("Không thể tải file", e);
                }
        }

        @Override
        public void delete(long id) {
                Media media = mediaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("File không tồn tại"));

                if (mediaRepository.existsInPostMedia(id)) {
                        throw new BadRequestException("Ảnh đang được sử dụng trong bài viết");
                }

                try {
                        if (media.getUploadFile() != null && !media.getUploadFile().isBlank()) {
                                Path uploadPath = getUploadPath();
                                Path filePath = uploadPath.resolve(media.getUploadFile()).normalize();
                                Files.deleteIfExists(filePath);
                        }

                        mediaRepository.delete(media);

                } catch (IOException e) {
                        throw new RuntimeException("Không thể xóa file");
                }
        }

        private ResMediaDTO toDTO(Media media) {
                ResMediaDTO response = new ResMediaDTO();
                response.setMediaId(media.getMediaId());
                response.setFileName(media.getFileName());
                response.setFilePath("/api/v1/media/" + media.getMediaId() + "/view");
                response.setMimeType(media.getMimeType());
                response.setFileType(media.getFileType());
                response.setFileSize(media.getFileSize());
                response.setMediaWidth(media.getMediaWidth());
                response.setMediaHeight(media.getMediaHeight());
                response.setUploadedBy(media.getUploadedBy());
                response.setUploadedAt(media.getUploadedAt());
                return response;
        }
}