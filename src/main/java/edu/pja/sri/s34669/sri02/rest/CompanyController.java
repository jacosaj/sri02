package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.CompanyDetailsDto;
import edu.pja.sri.s34669.sri02.dto.CompanyDto;
import edu.pja.sri.s34669.sri02.dto.EmployeeDto;
import edu.pja.sri.s34669.sri02.model.Company;
import edu.pja.sri.s34669.sri02.model.Employee;
import edu.pja.sri.s34669.sri02.repo.CompanyRepository;
import edu.pja.sri.s34669.sri02.repo.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    // --- helpers ---

    private EntityModel<CompanyDto> toModel(Company company) {
        CompanyDto dto = modelMapper.map(company, CompanyDto.class);
        return EntityModel.of(dto,
                linkTo(methodOn(CompanyController.class).getCompanyById(company.getId())).withSelfRel(),
                linkTo(methodOn(CompanyController.class).getCompanyEmployees(company.getId())).withRel("employees"));
    }

    private EntityModel<EmployeeDto> toEmployeeModel(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);
        return EntityModel.of(dto,
                linkTo(methodOn(EmployeeController.class).getEmployeeById(employee.getId())).withSelfRel());
    }

    // --- 2. basic CRUD ---

    // GET /api/companies
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<CompanyDto>>> getAllCompanies() {
        List<EntityModel<CompanyDto>> companies = companyRepository.findAll().stream()
                .map(this::toModel)
                .toList();
        CollectionModel<EntityModel<CompanyDto>> result = CollectionModel.of(companies,
                linkTo(methodOn(CompanyController.class).getAllCompanies()).withSelfRel());
        return ResponseEntity.ok(result);
    }

    // 3a. GET /api/companies/{id}  — company WITH employees
    @GetMapping("/{companyId}")
    public ResponseEntity<EntityModel<CompanyDetailsDto>> getCompanyById(@PathVariable Long companyId) {
        Optional<Company> opt = companyRepository.findById(companyId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Company company = opt.get();
        CompanyDetailsDto dto = new CompanyDetailsDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        Set<EmployeeDto> employeeDtos = employeeRepository.findByEmployer_Id(companyId).stream()
                .map(e -> modelMapper.map(e, EmployeeDto.class))
                .collect(Collectors.toSet());
        dto.setEmployees(employeeDtos);

        EntityModel<CompanyDetailsDto> model = EntityModel.of(dto,
                linkTo(methodOn(CompanyController.class).getCompanyById(companyId)).withSelfRel(),
                linkTo(methodOn(CompanyController.class).getCompanyEmployees(companyId)).withRel("employees"));
        return ResponseEntity.ok(model);
    }

    // POST /api/companies
    @PostMapping
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDto dto) {
        Company company = Company.builder().name(dto.getName()).build();
        companyRepository.save(company);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(company.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    // PUT /api/companies/{id}
    @PutMapping("/{companyId}")
    public ResponseEntity<?> updateCompany(@PathVariable Long companyId, @Valid @RequestBody CompanyDto dto) {
        if (!companyRepository.existsById(companyId)) return ResponseEntity.notFound().build();
        Company company = Company.builder().id(companyId).name(dto.getName()).build();
        companyRepository.save(company);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/companies/{id}
    @DeleteMapping("/{companyId}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) return ResponseEntity.notFound().build();
        // unlink employees before deleting
        employeeRepository.findByEmployer_Id(companyId).forEach(e -> {
            e.setEmployer(null);
            employeeRepository.save(e);
        });
        companyRepository.deleteById(companyId);
        return ResponseEntity.noContent().build();
    }

    // --- 3b. GET /api/companies/{id}/employees — ONLY related employees ---

    @GetMapping("/{companyId}/employees")
    public ResponseEntity<CollectionModel<EntityModel<EmployeeDto>>> getCompanyEmployees(
            @PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) return ResponseEntity.notFound().build();

        List<EntityModel<EmployeeDto>> employees = employeeRepository.findByEmployer_Id(companyId).stream()
                .map(this::toEmployeeModel)
                .toList();
        CollectionModel<EntityModel<EmployeeDto>> result = CollectionModel.of(employees,
                linkTo(methodOn(CompanyController.class).getCompanyEmployees(companyId)).withSelfRel(),
                linkTo(methodOn(CompanyController.class).getCompanyById(companyId)).withRel("company"));
        return ResponseEntity.ok(result);
    }

    // --- 3c. POST /api/companies/{companyId}/employees/{empId} — create relationship ---

    @PostMapping("/{companyId}/employees/{empId}")
    public ResponseEntity<?> addEmployeeToCompany(@PathVariable Long companyId, @PathVariable Long empId) {
        Optional<Company> company = companyRepository.findById(companyId);
        Optional<Employee> employee = employeeRepository.findById(empId);
        if (company.isEmpty() || employee.isEmpty()) return ResponseEntity.notFound().build();

        Employee e = employee.get();
        e.setEmployer(company.get());
        employeeRepository.save(e);
        return ResponseEntity.noContent().build();
    }

    // --- 3d. DELETE /api/companies/{companyId}/employees/{empId} — remove relationship ---

    @DeleteMapping("/{companyId}/employees/{empId}")
    public ResponseEntity<?> removeEmployeeFromCompany(@PathVariable Long companyId, @PathVariable Long empId) {
        Optional<Employee> employee = employeeRepository.findById(empId);
        if (employee.isEmpty()) return ResponseEntity.notFound().build();

        Employee e = employee.get();
        if (e.getEmployer() == null || !e.getEmployer().getId().equals(companyId)) {
            return ResponseEntity.notFound().build();
        }
        e.setEmployer(null);
        employeeRepository.save(e);
        return ResponseEntity.noContent().build();
    }
}
