package IVS.CMS.services;

import IVS.CMS.services.dto.request.ReqCollaborator;
import IVS.CMS.services.dto.response.ResCollaborator;

import java.util.List;

public interface CollaboratorService {

    List<ResCollaborator> getCollaborator();

    ResCollaborator findById(Long id);

    ResCollaborator create(ReqCollaborator request);

    ResCollaborator update(Long id, ReqCollaborator request);

    void delete(Long id);
}