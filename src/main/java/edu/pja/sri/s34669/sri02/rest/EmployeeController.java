package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.EmployeeDto;
import edu.pja.sri.s34669.sri02.model.Employee;
import edu.pja.sri.s34669.sri02.repo.EmployeeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    private EntityModel<EmployeeDto> toModel(Employee employee) {
        EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);
        EntityModel<EmployeeDto> model = EntityModel.of(dto,
                linkTo(methodOn(EmployeeController.class).getEmployeeById(employee.getId())).withSelfRel());
        if (employee.getEmployer() != null) {
            model.add(linkTo(methodOn(CompanyController.class)
                    .getCompanyById(employee.getEmployer().getId())).withRel("employer"));
        }
        return model;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EmployeeDto>>> getEmployees() {
        List<EntityModel<EmployeeDto>> employees = employeeRepository.findAll().stream()
                .map(this::toModel)
                .toList();
        CollectionModel<EntityModel<EmployeeDto>> result = CollectionModel.of(employees,
                linkTo(methodOn(EmployeeController.class).getEmployees()).withSelfRel());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{empId}")
    public ResponseEntity<EntityModel<EmployeeDto>> getEmployeeById(@PathVariable Long empId) {
        return employeeRepository.findById(empId)
                .map(e -> ResponseEntity.ok(toModel(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> saveNewEmployee(@Valid @RequestBody EmployeeDto emp) {
        Employee entity = modelMapper.map(emp, Employee.class);
        entity.setId(null);
        employeeRepository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(entity.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{empId}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long empId, @Valid @RequestBody EmployeeDto dto) {
        if (!employeeRepository.existsById(empId)) return ResponseEntity.notFound().build();
        dto.setId(empId);
        employeeRepository.save(modelMapper.map(dto, Employee.class));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{empId}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long empId) {
        if (!employeeRepository.existsById(empId)) return ResponseEntity.notFound().build();
        employeeRepository.deleteById(empId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
