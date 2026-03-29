package edu.pja.sri.s34669.sri02.repo;

import edu.pja.sri.s34669.sri02.model.Patient;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PatientRepository extends CrudRepository<Patient, Long> {
    List<Patient> findAll();
}
