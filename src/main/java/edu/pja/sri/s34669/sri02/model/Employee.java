package edu.pja.sri.s34669.sri02.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String job;


    @ManyToOne
    @JoinColumn(name = "employer_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Company employer;
}
