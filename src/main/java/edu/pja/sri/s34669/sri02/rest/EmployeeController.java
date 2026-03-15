package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.EmployeeDto;
import edu.pja.sri.s34669.sri02.model.Employee;
import edu.pja.sri.s34669.sri02.repo.EmployeeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private EmployeeRepository employeeRepository;
    private ModelMapper modelMapper;

    public EmployeeController(EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    private EmployeeDto convertToDto(Employee e) {
        return modelMapper.map(e, EmployeeDto.class);
    }
    private Employee convertToEntity(EmployeeDto dto) {
        return modelMapper.map(dto, Employee.class);
    }

    @GetMapping
    public ResponseEntity<Collection<EmployeeDto>> getEmployees() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<EmployeeDto> result = allEmployees.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @GetMapping("/{empId}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long empId) {
        Optional<Employee> emp = employeeRepository.findById(empId);
        if (emp.isPresent()) {
            EmployeeDto employeeDto = convertToDto(emp.get());
            return new ResponseEntity<>(employeeDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity saveNewEmployee(@RequestBody EmployeeDto emp) {
        Employee entity = convertToEntity(emp);
        employeeRepository.save(entity);
        HttpHeaders headers = new HttpHeaders();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(entity.getId())
                .toUri();
        headers.add("Location", location.toString());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping("/{empId}")
    public ResponseEntity updateEmployee(@PathVariable Long empId,
                                         @RequestBody EmployeeDto employeeDto) {
        Optional<Employee> currentEmp = employeeRepository.findById(empId);
        if (currentEmp.isPresent()) {
            employeeDto.setId(empId);
            Employee entity = convertToEntity(employeeDto);
            employeeRepository.save(entity);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{empId}")
    public ResponseEntity deleteEmployee(@PathVariable Long empId) {
        boolean found = employeeRepository.existsById(empId);
        if (found) {
            employeeRepository.deleteById(empId);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

}
