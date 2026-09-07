package IVS.CMS.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import IVS.CMS.services.dto.response.ResMediaDTO;

public interface MediaService {
    ResMediaDTO upload(MultipartFile file);

    ResponseEntity<Resource> view(long mediaId);

    ResponseEntity<Resource> download(long mediaId);

    List<ResMediaDTO> getAllMedia();

    List<ResMediaDTO> searchAndFilter(String keyword, String fileType);

    void delete(long mediaId);
}
