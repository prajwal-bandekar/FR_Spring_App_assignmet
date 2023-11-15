package org.freshreview.FR_Springboot_App.repository;

import org.freshreview.FR_Springboot_App.dto.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	
    Optional<Employee> findByFirstNameAndLastName(String firstName, String lastName);
    
    boolean existsByUniqueId(String uniqueId);
}

