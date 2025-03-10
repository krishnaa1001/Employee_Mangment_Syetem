package in.krrishit.repository;

import in.krrishit.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByProjectIdContainingOrProjectNameContaining(String projectId, String projectName, Pageable pageable);
}
