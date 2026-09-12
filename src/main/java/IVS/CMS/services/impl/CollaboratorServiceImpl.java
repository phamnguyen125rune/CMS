package IVS.CMS.services.impl;

import IVS.CMS.domain.Collaborator;
import IVS.CMS.repositories.CollaboratorRepository;
import IVS.CMS.services.CollaboratorService;
import IVS.CMS.services.dto.request.ReqCollaborator;
import IVS.CMS.services.dto.response.ResCollaborator;
import IVS.CMS.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaboratorServiceImpl implements CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;

    @Override
    public List<ResCollaborator> getCollaborator() {
        return collaboratorRepository.getCollaborator()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ResCollaborator findById(Long id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collaborator không tồn tại"));

        return toResponse(collaborator);
    }

    @Override
    public ResCollaborator create(ReqCollaborator request) {
        Collaborator collaborator = new Collaborator();

        collaborator.setCollabName(request.getCollabName());
        collaborator.setDescription(request.getDescription());
        collaborator.setPosition(request.getPosition());
        collaborator.setCompanyImage(request.getCompanyImage());
        collaborator.setVisible(
                request.getVisible() != null ? request.getVisible() : true
        );

        LocalDateTime now = LocalDateTime.now();
        collaborator.setCreatedAt(now);

        SecurityService.getCurrentUserId().ifPresent(collaborator::setCreatedBy);

        collaboratorRepository.save(collaborator);

        return toResponse(collaborator);
    }

    @Override
    public ResCollaborator update(Long id, ReqCollaborator request) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collaborator không tồn tại"));

        collaborator.setCollabName(request.getCollabName());
        collaborator.setDescription(request.getDescription());
        collaborator.setPosition(request.getPosition());
        collaborator.setCompanyImage(request.getCompanyImage());

        collaborator.setVisible(
                request.getVisible() != null
                        ? request.getVisible()
                        : collaborator.getVisible()
        );

        collaborator.setUpdatedAt(LocalDateTime.now());

        SecurityService.getCurrentUserId().ifPresent(collaborator::setUpdatedBy);

        collaboratorRepository.update(collaborator);

        return toResponse(collaborator);
    }

    @Override
    public void delete(Long id) {
        collaboratorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collaborator không tồn tại"));

        collaboratorRepository.deleteById(id);
    }

    private ResCollaborator toResponse(Collaborator collaborator) {
        ResCollaborator response = new ResCollaborator();

        response.setCollabId(collaborator.getCollabId());
        response.setCollabName(collaborator.getCollabName());
        response.setDescription(collaborator.getDescription());
        response.setPosition(collaborator.getPosition());
        response.setCompanyImage(collaborator.getCompanyImage());
        response.setVisible(collaborator.getVisible());
        response.setCreatedAt(collaborator.getCreatedAt());
        response.setCreatedBy(collaborator.getCreatedBy());
        response.setUpdatedAt(collaborator.getUpdatedAt());
        response.setUpdatedBy(collaborator.getUpdatedBy());

        return response;
    }
}