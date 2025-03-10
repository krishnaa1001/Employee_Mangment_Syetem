package in.krrishit.repository;

import in.krrishit.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByEmail(String email);
    Page<Client> findByClientIdContainingOrCompanyNameContaining(String clientId, String companyName, Pageable pageable);
}
