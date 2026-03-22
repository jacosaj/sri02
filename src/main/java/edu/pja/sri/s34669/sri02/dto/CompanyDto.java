package edu.pja.sri.s34669.sri02.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;
}
