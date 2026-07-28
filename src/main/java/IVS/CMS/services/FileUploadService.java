package IVS.CMS.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String uploadAvatar(MultipartFile file);

    void deleteAvatar(String filePath);
}