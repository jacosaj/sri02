package edu.pja.sri.s34669.sri02.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;
    private String message;
    private List<FieldErrorDetail> errors;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String message;
    }
}
