package edu.pja.sri.s34669.sri02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publishedYear;
    private Double price;
}
