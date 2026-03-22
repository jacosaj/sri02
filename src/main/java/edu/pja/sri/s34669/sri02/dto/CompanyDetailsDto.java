package edu.pja.sri.s34669.sri02.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDetailsDto {
    private Long id;
    private String name;
    private Set<EmployeeDto> employees;
}
