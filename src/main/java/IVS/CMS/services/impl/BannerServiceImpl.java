package IVS.CMS.services.impl;

import IVS.CMS.domain.Banner;
import IVS.CMS.repositories.BannerRepository;
import IVS.CMS.services.BannerService;
import IVS.CMS.services.dto.request.ReqCreateBannerDTO;
import IVS.CMS.services.dto.request.ReqUpdateBannerDTO;
import IVS.CMS.services.dto.response.ResBannerDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public List<ResBannerDTO> getActiveBanners(String position) {
        List<Banner> banners = bannerRepository.findActiveByPosition(position);
        return banners.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ResultPaginationDTO getAllBanners(String search, String position, Boolean isActive, int page, int size) {
        long total = bannerRepository.count(search, position, isActive);
        List<Banner> list = bannerRepository.findAll(search, position, isActive, page, size);
        List<ResBannerDTO> dtos = list.stream().map(this::mapToDTO).collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(size);
        meta.setPages((int) Math.ceil((double) total / size));
        meta.setTotal(total);

        ResultPaginationDTO response = new ResultPaginationDTO();
        response.setMeta(meta);
        response.setResult(dtos);
        return response;
    }

    @Override
    public ResBannerDTO getBannerById(Long id) {
        Optional<Banner> opt = bannerRepository.findById(id);
        return opt.map(this::mapToDTO).orElse(null);
    }

    @Override
    public ResBannerDTO createBanner(ReqCreateBannerDTO dto, Long userId) {
        Banner banner = new Banner();
        banner.setTitle(trimToNull(dto.getTitle()));
        banner.setHighlightText(trimToNull(dto.getHighlightText()));
        banner.setSubtitle(trimToNull(dto.getSubtitle()));
        banner.setDescription(trimToNull(dto.getDescription()));
        banner.setImageUrl(trimToNull(dto.getImageUrl()));
        banner.setMobileImageUrl(trimToNull(dto.getMobileImageUrl()));
        banner.setPrimaryBtnText(trimToNull(dto.getPrimaryBtnText()));
        banner.setPrimaryBtnUrl(trimToNull(dto.getPrimaryBtnUrl()));
        banner.setSecondaryBtnText(trimToNull(dto.getSecondaryBtnText()));
        banner.setSecondaryBtnUrl(trimToNull(dto.getSecondaryBtnUrl()));
        banner.setStatsJson(trimToNull(dto.getStatsJson()));
        banner.setFloatingBadgeText(trimToNull(dto.getFloatingBadgeText()));
        banner.setPosition(dto.getPosition() != null ? dto.getPosition().trim() : "HOME_HERO");
        banner.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        banner.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        banner.setCreatedBy(userId);

        Banner saved = bannerRepository.save(banner);
        return mapToDTO(saved);
    }

    @Override
    public ResBannerDTO updateBanner(Long id, ReqUpdateBannerDTO dto, Long userId) {
        Optional<Banner> opt = bannerRepository.findById(id);
        if (opt.isEmpty()) {
            return null;
        }

        Banner banner = opt.get();
        banner.setTitle(trimToNull(dto.getTitle()));
        banner.setHighlightText(trimToNull(dto.getHighlightText()));
        banner.setSubtitle(trimToNull(dto.getSubtitle()));
        banner.setDescription(trimToNull(dto.getDescription()));
        banner.setImageUrl(trimToNull(dto.getImageUrl()));
        banner.setMobileImageUrl(trimToNull(dto.getMobileImageUrl()));
        banner.setPrimaryBtnText(trimToNull(dto.getPrimaryBtnText()));
        banner.setPrimaryBtnUrl(trimToNull(dto.getPrimaryBtnUrl()));
        banner.setSecondaryBtnText(trimToNull(dto.getSecondaryBtnText()));
        banner.setSecondaryBtnUrl(trimToNull(dto.getSecondaryBtnUrl()));
        banner.setStatsJson(trimToNull(dto.getStatsJson()));
        banner.setFloatingBadgeText(trimToNull(dto.getFloatingBadgeText()));
        if (dto.getPosition() != null) {
            banner.setPosition(dto.getPosition().trim());
        }
        if (dto.getDisplayOrder() != null) {
            banner.setDisplayOrder(dto.getDisplayOrder());
        }
        if (dto.getIsActive() != null) {
            banner.setIsActive(dto.getIsActive());
        }
        banner.setUpdatedBy(userId);

        Banner updated = bannerRepository.update(banner);
        return mapToDTO(updated);
    }

    @Override
    public boolean deleteBanner(Long id) {
        return bannerRepository.delete(id);
    }

    @Override
    public boolean toggleStatus(Long id, boolean isActive) {
        return bannerRepository.updateStatus(id, isActive);
    }

    private ResBannerDTO mapToDTO(Banner banner) {
        if (banner == null) return null;
        ResBannerDTO dto = new ResBannerDTO();
        dto.setBannerId(banner.getBannerId());
        dto.setTitle(banner.getTitle());
        dto.setHighlightText(banner.getHighlightText());
        dto.setSubtitle(banner.getSubtitle());
        dto.setDescription(banner.getDescription());
        dto.setImageUrl(banner.getImageUrl());
        dto.setMobileImageUrl(banner.getMobileImageUrl());
        dto.setPrimaryBtnText(banner.getPrimaryBtnText());
        dto.setPrimaryBtnUrl(banner.getPrimaryBtnUrl());
        dto.setSecondaryBtnText(banner.getSecondaryBtnText());
        dto.setSecondaryBtnUrl(banner.getSecondaryBtnUrl());
        dto.setStatsJson(banner.getStatsJson());
        dto.setFloatingBadgeText(banner.getFloatingBadgeText());
        dto.setPosition(banner.getPosition());
        dto.setDisplayOrder(banner.getDisplayOrder());
        dto.setIsActive(banner.getIsActive());
        dto.setCreatedAt(banner.getCreatedAt());
        dto.setCreatedBy(banner.getCreatedBy());
        dto.setUpdatedAt(banner.getUpdatedAt());
        dto.setUpdatedBy(banner.getUpdatedBy());
        return dto;
    }

    private String trimToNull(String val) {
        if (val == null) return null;
        String t = val.trim();
        return t.isEmpty() ? null : t;
    }
}
