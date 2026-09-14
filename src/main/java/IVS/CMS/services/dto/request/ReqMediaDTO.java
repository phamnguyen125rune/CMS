package IVS.CMS.services.dto.request;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqMediaDTO {
    private MultipartFile file;
}