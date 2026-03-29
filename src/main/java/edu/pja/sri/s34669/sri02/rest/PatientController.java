package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.PatientDto;
import edu.pja.sri.s34669.sri02.model.Patient;
import edu.pja.sri.s34669.sri02.repo.PatientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    private EntityModel<PatientDto> toModel(Patient patient) {
        PatientDto dto = modelMapper.map(patient, PatientDto.class);
        return EntityModel.of(dto,
                linkTo(methodOn(PatientController.class).getPatientById(patient.getId())).withSelfRel(),
                linkTo(methodOn(PatientController.class).getPatients()).withRel("patients"));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PatientDto>>> getPatients() {
        List<EntityModel<PatientDto>> patients = patientRepository.findAll().stream()
                .map(this::toModel)
                .toList();
        CollectionModel<EntityModel<PatientDto>> result = CollectionModel.of(patients,
                linkTo(methodOn(PatientController.class).getPatients()).withSelfRel());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<EntityModel<PatientDto>> getPatientById(@PathVariable Long patientId) {
        return patientRepository.findById(patientId)
                .map(p -> ResponseEntity.ok(toModel(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> saveNewPatient(@Valid @RequestBody PatientDto dto) {
        Patient entity = modelMapper.map(dto, Patient.class);
        entity.setId(null);
        patientRepository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(entity.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{patientId}")
    public ResponseEntity<?> updatePatient(@PathVariable Long patientId, @Valid @RequestBody PatientDto dto) {
        if (!patientRepository.existsById(patientId)) return ResponseEntity.notFound().build();
        dto.setId(patientId);
        patientRepository.save(modelMapper.map(dto, Patient.class));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{patientId}")
    public ResponseEntity<?> deletePatient(@PathVariable Long patientId) {
        if (!patientRepository.existsById(patientId)) return ResponseEntity.notFound().build();
        patientRepository.deleteById(patientId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
