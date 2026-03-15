package edu.pja.sri.s34669.sri02.repo;

import edu.pja.sri.s34669.sri02.model.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    List<Employee> findAll();
}
