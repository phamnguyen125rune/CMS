package IVS.CMS.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.services.FileUploadService;
import IVS.CMS.services.error.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp");

    @Override
    public String uploadAvatar(MultipartFile file) {
        validateFile(file);

        try {
            String fileName = generateFileName(file);
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName).normalize();
            if (!filePath.startsWith(uploadPath)) {
                throw new BadRequestException("Tên file không hợp lệ");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteAvatar(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path path = uploadPath.resolve(fileName).normalize();

            if (!path.startsWith(uploadPath)) {
                throw new BadRequestException("Đường dẫn file không hợp lệ");
            }

            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi xóa file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File quá lớn. Giới hạn: 5MB");
        }

        if (!isAllowedImageType(file.getContentType())) {
            throw new BadRequestException("Loại file không hỗ trợ. Chỉ hỗ trợ: JPEG, PNG, GIF, WebP");
        }

        String extension = getSafeExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Đuôi file không hợp lệ. Chỉ hỗ trợ: jpg, jpeg, png, gif, webp");
        }
    }

    private String generateFileName(MultipartFile file) {
        String extension = getSafeExtension(file.getOriginalFilename());
        return UUID.randomUUID() + extension;
    }

    private String getSafeExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BadRequestException("Tên file không hợp lệ");
        }

        String safeName = Paths.get(originalFileName).getFileName().toString();
        int dotIndex = safeName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == safeName.length() - 1) {
            throw new BadRequestException("File phải có phần mở rộng hợp lệ");
        }

        return safeName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedImageType(String contentType) {
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }
}
