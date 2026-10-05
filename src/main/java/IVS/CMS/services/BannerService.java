package IVS.CMS.services;

import IVS.CMS.domain.Banner;
import IVS.CMS.services.dto.request.ReqCreateBannerDTO;
import IVS.CMS.services.dto.request.ReqUpdateBannerDTO;
import IVS.CMS.services.dto.response.ResBannerDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;

import java.util.List;

public interface BannerService {
    List<ResBannerDTO> getActiveBanners(String position);
    ResultPaginationDTO getAllBanners(String search, String position, Boolean isActive, int page, int size);
    ResBannerDTO getBannerById(Long id);
    ResBannerDTO createBanner(ReqCreateBannerDTO dto, Long userId);
    ResBannerDTO updateBanner(Long id, ReqUpdateBannerDTO dto, Long userId);
    boolean deleteBanner(Long id);
    boolean toggleStatus(Long id, boolean isActive);
}
