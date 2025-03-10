package in.krrishit.repository;

import in.krrishit.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByEmail(String email);
    Page<Employee> findByEmpIdContainingOrNameContaining(String empId, String name, Pageable pageable);
}
