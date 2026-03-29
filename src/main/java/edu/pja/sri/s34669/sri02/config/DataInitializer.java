package edu.pja.sri.s34669.sri02.config;

import edu.pja.sri.s34669.sri02.model.AppUser;
import edu.pja.sri.s34669.sri02.model.Company;
import edu.pja.sri.s34669.sri02.model.Employee;
import edu.pja.sri.s34669.sri02.model.Patient;
import edu.pja.sri.s34669.sri02.repo.AppUserRepository;
import edu.pja.sri.s34669.sri02.repo.CompanyRepository;
import edu.pja.sri.s34669.sri02.repo.EmployeeRepository;
import edu.pja.sri.s34669.sri02.repo.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AppUserRepository appUserRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (companyRepository.count() == 0) {
            Company acme = companyRepository.save(Company.builder().name("Acme Corp").build());
            Company globex = companyRepository.save(Company.builder().name("Globex").build());

            employeeRepository.save(new Employee(null, "Jan", "Kowalski", LocalDate.of(1990, 5, 15), "Developer", acme));
            employeeRepository.save(new Employee(null, "Anna", "Nowak", LocalDate.of(1985, 8, 22), "Manager", acme));
            employeeRepository.save(new Employee(null, "Piotr", "Wiśniewski", LocalDate.of(1992, 3, 10), "Analyst", globex));
        }

        if (appUserRepository.count() == 0) {
            appUserRepository.save(new AppUser(null,
                    "admin@pandas.pl",
                    passwordEncoder.encode("admin123"),
                    "ROLE_ADMIN"));

            appUserRepository.save(new AppUser(null,
                    "user@pandas.pl",
                    passwordEncoder.encode("user123"),
                    "ROLE_USER"));
        }

        if (patientRepository.count() == 0) {
            patientRepository.save(new Patient(null, "Marek", "Kowalski",
                    LocalDate.of(2015, 3, 10), "rodzic@example.pl", "600100200"));
            patientRepository.save(new Patient(null, "Zofia", "Nowak",
                    LocalDate.of(2018, 7, 22), "mama@example.pl", "601200300"));
        }
    }
}
