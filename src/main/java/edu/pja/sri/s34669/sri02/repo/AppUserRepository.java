package edu.pja.sri.s34669.sri02.repo;

import edu.pja.sri.s34669.sri02.model.AppUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
