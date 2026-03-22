package edu.pja.sri.s34669.sri02.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @Min(value = 1000, message = "Published year must be at least 1000")
    @Max(value = 2100, message = "Published year must be at most 2100")
    private Integer publishedYear;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private Double price;
}
