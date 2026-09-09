package IVS.CMS.services.impl;

import IVS.CMS.domain.Menu;
import IVS.CMS.repositories.MenuRepository;
import IVS.CMS.services.MenuService;
import IVS.CMS.services.dto.request.ReqMenuDTO;
import IVS.CMS.services.dto.response.ResMenuDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    @Override
    public List<ResMenuDTO> getMenu() {
        return menuRepository.getMenu()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ResMenuDTO findById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu không tồn tại"));

        return toResponse(menu);
    }

    @Override
    public ResMenuDTO create(ReqMenuDTO request) {
        Menu menu = new Menu();

        menu.setParentId(request.getParentId());
        menu.setTitle(request.getTitle());
        menu.setUrl(request.getUrl());
        menu.setDisplayOrder(request.getDisplayOrder());
        menu.setLevel(request.getLevel());
        menu.setVisible(request.getVisible());

        LocalDateTime now = LocalDateTime.now();
        menu.setCreatedAt(now);

        menuRepository.save(menu);

        return toResponse(menu);
    }

    @Override
    public ResMenuDTO update(Long id, ReqMenuDTO request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu không tồn tại"));

        menu.setParentId(request.getParentId());
        menu.setTitle(request.getTitle());
        menu.setUrl(request.getUrl());
        menu.setDisplayOrder(request.getDisplayOrder());
        menu.setLevel(request.getLevel());
        menu.setVisible(request.getVisible());
        menu.setUpdatedAt(LocalDateTime.now());

        menuRepository.update(menu);

        return toResponse(menu);
    }

    @Override
    public void delete(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu không tồn tại"));

        menuRepository.deleteById(id);
    }

    private ResMenuDTO toResponse(Menu menu) {
        ResMenuDTO response = new ResMenuDTO();

        response.setMenuId(menu.getMenuId());
        response.setParentId(menu.getParentId());
        response.setTitle(menu.getTitle());
        response.setUrl(menu.getUrl());
        response.setDisplayOrder(menu.getDisplayOrder());
        response.setLevel(menu.getLevel());
        response.setVisible(menu.getVisible());
        response.setCreatedAt(menu.getCreatedAt());
        response.setCreatedBy(menu.getCreatedBy());
        response.setUpdatedAt(menu.getUpdatedAt());
        response.setUpdatedBy(menu.getUpdatedBy());

        return response;
    }
}