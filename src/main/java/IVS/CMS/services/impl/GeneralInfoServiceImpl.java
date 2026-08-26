package IVS.CMS.services.impl;

import IVS.CMS.domain.GeneralInfo;
import IVS.CMS.repositories.GeneralInfoRepository;
import IVS.CMS.services.GeneralInfoService;
import IVS.CMS.services.dto.request.ReqUpdateGeneralInfoDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GeneralInfoServiceImpl implements GeneralInfoService {

    private final GeneralInfoRepository repository;

    public GeneralInfoServiceImpl(GeneralInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public GeneralInfo getGeneralInfo() {
        Optional<GeneralInfo> opt = repository.findFirst();
        // Nếu chưa có bản ghi nào trong DB, trả về một object rỗng mặc định tránh lỗi Null
        return opt.orElseGet(GeneralInfo::new);
    }

    @Override
    public GeneralInfo saveOrUpdateGeneralInfo(ReqUpdateGeneralInfoDTO dto, Long userId) {
        Optional<GeneralInfo> opt = repository.findFirst();
        if (opt.isPresent()) {
            GeneralInfo info = opt.get();
            info.setLogo(dto.getLogo() != null ? dto.getLogo() : info.getLogo());
            info.setCompanyName(dto.getCompanyName());
            info.setWebsiteName(dto.getWebsiteName());
            info.setWebsiteDescription(dto.getWebsiteDescription());
            info.setEmail(dto.getEmail());
            info.setFacebookLink(dto.getFacebookLink());
            info.setTwitterLink(dto.getTwitterLink());
            info.setInstagramLink(dto.getInstagramLink());
            info.setLinkedinLink(dto.getLinkedinLink());
            info.setYoutubeLink(dto.getYoutubeLink());
            info.setZaloLink(dto.getZaloLink());
            info.setCompanyPhoneNumber(dto.getCompanyPhoneNumber());
            info.setUpdatedBy(userId);
            return repository.update(info);
        } else {
            GeneralInfo info = new GeneralInfo();
            info.setLogo(dto.getLogo() != null ? dto.getLogo() : "default.png");
            info.setCompanyName(dto.getCompanyName());
            info.setWebsiteName(dto.getWebsiteName());
            info.setWebsiteDescription(dto.getWebsiteDescription());
            info.setEmail(dto.getEmail());
            info.setFacebookLink(dto.getFacebookLink());
            info.setTwitterLink(dto.getTwitterLink());
            info.setInstagramLink(dto.getInstagramLink());
            info.setLinkedinLink(dto.getLinkedinLink());
            info.setYoutubeLink(dto.getYoutubeLink());
            info.setZaloLink(dto.getZaloLink());
            info.setCompanyPhoneNumber(dto.getCompanyPhoneNumber());
            info.setCreatedBy(userId);
            return repository.save(info);
        }
    }
}