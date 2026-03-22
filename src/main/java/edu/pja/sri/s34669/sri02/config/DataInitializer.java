package edu.pja.sri.s34669.sri02.config;

import edu.pja.sri.s34669.sri02.model.Company;
import edu.pja.sri.s34669.sri02.model.Employee;
import edu.pja.sri.s34669.sri02.repo.CompanyRepository;
import edu.pja.sri.s34669.sri02.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() == 0) {
            Company acme = companyRepository.save(Company.builder().name("Acme Corp").build());
            Company globex = companyRepository.save(Company.builder().name("Globex").build());

            employeeRepository.save(new Employee(null, "Jan", "Kowalski", LocalDate.of(1990, 5, 15), "Developer", acme));
            employeeRepository.save(new Employee(null, "Anna", "Nowak", LocalDate.of(1985, 8, 22), "Manager", acme));
            employeeRepository.save(new Employee(null, "Piotr", "Wiśniewski", LocalDate.of(1992, 3, 10), "Analyst", globex));
        }
    }
}
