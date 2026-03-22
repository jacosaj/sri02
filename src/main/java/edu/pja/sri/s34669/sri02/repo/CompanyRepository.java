package edu.pja.sri.s34669.sri02.repo;

import edu.pja.sri.s34669.sri02.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}
