package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Collaborator;

@Repository
public interface CollaboratorRepository {

    List<Collaborator> getCollaborator();

    Optional<Collaborator> findById(Long id);

    int save(Collaborator collaborator);

    int update(Collaborator collaborator);

    int deleteById(Long id);
}