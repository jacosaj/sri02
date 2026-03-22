package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.CompanyDetailsDto;
import edu.pja.sri.s34669.sri02.dto.CompanyDto;
import edu.pja.sri.s34669.sri02.model.Company;
import edu.pja.sri.s34669.sri02.repo.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        List<Company> all = companyRepository.findAll();
        List<CompanyDto> result = all.stream()
                .map(company -> modelMapper.map(company, CompanyDto.class))
                .toList();

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDetailsDto> getCompanyByIdWithEmployees(@PathVariable Long companyId) {
        Optional<Company> company = companyRepository.findById(companyId);

        if (company.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(modelMapper.map(company.get(), CompanyDetailsDto.class));
    }
}