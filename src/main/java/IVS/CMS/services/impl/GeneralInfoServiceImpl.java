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

    public GeneralInfoServiceImpl(
            GeneralInfoRepository repository) {

        this.repository = repository;
    }

    @Override
    public GeneralInfo getGeneralInfo() {

        Optional<GeneralInfo> opt = repository.findFirst();

        return opt.orElseGet(GeneralInfo::new);
    }

    @Override
    public GeneralInfo saveOrUpdateGeneralInfo(
            ReqUpdateGeneralInfoDTO dto,
            Long userId) {

        Optional<GeneralInfo> opt = repository.findFirst();

        if (opt.isPresent()) {

            GeneralInfo info = opt.get();

            mapDataGlobeInfo(info, dto);

            return repository.update(info);
        }

        GeneralInfo info = new GeneralInfo();

        mapDataGlobeInfo(info, dto);

        info.setCreatedBy(userId);

        return repository.save(info);
    }

    private void mapDataGlobeInfo(GeneralInfo info, ReqUpdateGeneralInfoDTO dto) {

        info.setLogo(
                trimToNull(dto.getLogo()));

        info.setCompanyName(
                trimToNull(dto.getCompanyName()));

        info.setWebsiteName(
                trimToNull(dto.getWebsiteName()));

        info.setWebsiteDescription(
                trimToNull(dto.getWebsiteDescription()));

        info.setEmail(
                trimToNull(dto.getEmail()));

        info.setFacebookLink(
                trimToNull(dto.getFacebookLink()));

        info.setTwitterLink(
                trimToNull(dto.getTwitterLink()));

        info.setInstagramLink(
                trimToNull(dto.getInstagramLink()));

        info.setLinkedinLink(
                trimToNull(dto.getLinkedinLink()));

        info.setYoutubeLink(
                trimToNull(dto.getYoutubeLink()));

        info.setZaloLink(
                trimToNull(dto.getZaloLink()));

        info.setCompanyPhoneNumber(
                trimToNull(dto.getCompanyPhoneNumber()));

        info.setAddress(
                trimToNull(dto.getAddress()));

        info.setWorkingHours(
                trimToNull(dto.getWorkingHours()));

        info.setMapEmbedUrl(
                trimToNull(dto.getMapEmbedUrl()));

        info.setFooterLinks(
                trimToNull(dto.getFooterLinks()));

        info.setShowTopbar(dto.getShowTopbar() != null ? dto.getShowTopbar() : true);
        info.setTopbarAnnouncementText(trimToNull(dto.getTopbarAnnouncementText()));
        info.setTopbarAnnouncementUrl(trimToNull(dto.getTopbarAnnouncementUrl()));
        info.setHeaderCtaText(trimToNull(dto.getHeaderCtaText()));
        info.setHeaderCtaUrl(trimToNull(dto.getHeaderCtaUrl()));
        info.setShowHeaderSearch(dto.getShowHeaderSearch() != null ? dto.getShowHeaderSearch() : true);
        info.setShowThemeToggle(dto.getShowThemeToggle() != null ? dto.getShowThemeToggle() : true);
        info.setShowLanguageSwitch(dto.getShowLanguageSwitch() != null ? dto.getShowLanguageSwitch() : true);

        info.setFooterCopyright(trimToNull(dto.getFooterCopyright()));
        info.setShowNewsletter(dto.getShowNewsletter() != null ? dto.getShowNewsletter() : true);
        info.setNewsletterTitle(trimToNull(dto.getNewsletterTitle()));
        info.setNewsletterDesc(trimToNull(dto.getNewsletterDesc()));
        info.setFooterColumnsJson(trimToNull(dto.getFooterColumnsJson()));
    }

    /**
     * Chuyển chuỗi rỗng hoặc chỉ chứa khoảng trắng thành null.
     */
    private String trimToNull(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}