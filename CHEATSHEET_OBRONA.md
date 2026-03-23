# CHEATSHEET - OBRONA SRI-02

## STACK TECHNOLOGICZNY
- **Spring Boot 3.5** + Java 21
- **Spring Data JPA** - warstwa persystencji (H2 in-memory)
- **Spring HATEOAS** - hiperłącza w odpowiedziach REST
- **Spring Validation** - walidacja DTOs (`@Valid`)
- **ModelMapper** - konwersja Entity ↔ DTO
- **Lombok** - eliminacja boilerplate (`@Data`, `@Builder`, etc.)

---

## ARCHITEKTURA (przepływ danych)

```
HTTP Request
    ↓
@RestController  (walidacja @Valid, mapowanie DTO↔Entity)
    ↓
Repository       (Spring Data JPA → SQL → H2)
    ↓
HTTP Response    (EntityModel / CollectionModel z HATEOAS)
```

**Pakiety:**
```
model/     → JPA Entities (Company, Employee, Book)
dto/       → Data Transfer Objects (z walidacją)
repo/      → Interfaces extending CrudRepository/JpaRepository
rest/      → @RestController + @RestControllerAdvice
config/    → DataInitializer (dane startowe)
```

---

## ENTITIES I RELACJE

### Company.java
```java
@Entity @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "employer")  // "employer" = pole w Employee
    @ToString.Exclude                   // zapobiega StackOverflow w toString()
    @EqualsAndHashCode.Exclude          // zapobiega problemom w equals()
    private Set<Employee> employees;
}
```

### Employee.java
```java
@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String job;

    @ManyToOne
    @JoinColumn(name = "employer_id")  // kolumna FK w tabeli EMPLOYEE
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Company employer;
}
```

### Book.java
```java
@Entity @Data @NoArgsConstructor @AllArgsConstructor
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publishedYear;
    private Double price;
}
```

### RELACJA Company ↔ Employee
- **Company** ma `@OneToMany(mappedBy = "employer")` → jedna firma, wielu pracowników
- **Employee** ma `@ManyToOne` + `@JoinColumn(name = "employer_id")` → właściciel relacji
- `mappedBy = "employer"` mówi JPA: "klucz obcy jest po stronie Employee, w polu 'employer'"
- `GenerationType.AUTO` vs `IDENTITY`: AUTO = strategia zależna od bazy (H2: sekwencja), IDENTITY = autoincrement kolumny

---

## DTOs I WALIDACJA

### CompanyDto.java
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class CompanyDto {
    private Long id;
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;
}
```

### EmployeeDto.java
```java
@Data @AllArgsConstructor @NoArgsConstructor
public class EmployeeDto {
    private Long id;
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    @NotBlank(message = "Job title is required")
    private String job;
}
```

### BookDto.java
```java
@Data @NoArgsConstructor @AllArgsConstructor
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
```

### CompanyDetailsDto.java (zagnieżdżone employees)
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class CompanyDetailsDto {
    private Long id;
    private String name;
    private Set<EmployeeDto> employees;  // zagnieżdżone DTO (nie Entity!)
}
```

### Tabela adnotacji walidacyjnych
| Adnotacja | Znaczenie |
|---|---|
| `@NotBlank` | nie null, nie "", nie "   " (dla String) |
| `@NotNull` | nie null (dowolny typ, String może być "") |
| `@Past` | data musi być w przeszłości |
| `@Size(min,max)` | długość Stringa |
| `@Min(value)` | minimalna wartość liczbowa (int/long) |
| `@Max(value)` | maksymalna wartość liczbowa |
| `@DecimalMin(value)` | minimalna wartość dziesiętna (double/BigDecimal) |

---

## REPOSITORIES

```java
// JpaRepository = CrudRepository + PagingAndSortingRepository
public interface CompanyRepository extends JpaRepository<Company, Long> { }

// CrudRepository - podstawowy CRUD bez paginacji
public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    List<Employee> findAll();
    List<Employee> findByEmployer_Id(Long employerId);  // derived query
    // "_" w nazwie = przejście przez relację: Employee.employer.id
}

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findAll();
}
```

**Derived Query** `findByEmployer_Id`:
- JPA czyta nazwę metody: `findBy` + `Employer` (pole) + `_` + `Id` (pole zagnieżdżone)
- Generuje SQL: `SELECT * FROM employee WHERE employer_id = ?`

---

## CONTROLLERS - WSZYSTKIE ENDPOINTY

### CompanyController (`/api/companies`)
| Metoda | URL | Body | Status | Opis |
|---|---|---|---|---|
| GET | `/api/companies` | - | 200 | lista firm (HATEOAS) |
| GET | `/api/companies/{id}` | - | 200/404 | szczegóły firmy + employees |
| POST | `/api/companies` | CompanyDto | 201 + Location | utwórz firmę |
| PUT | `/api/companies/{id}` | CompanyDto | 204 | zaktualizuj firmę |
| DELETE | `/api/companies/{id}` | - | 204 | usuń firmę (odłącza employees) |
| GET | `/api/companies/{id}/employees` | - | 200 | employees firmy |
| POST | `/api/companies/{id}/employees/{empId}` | - | 204 | przypisz employee do firmy |
| DELETE | `/api/companies/{id}/employees/{empId}` | - | 204 | odłącz employee od firmy |

### EmployeeController (`/api/employees`)
| Metoda | URL | Body | Status | Opis |
|---|---|---|---|---|
| GET | `/api/employees` | - | 200 | lista employees |
| GET | `/api/employees/{id}` | - | 200/404 | szczegóły employee |
| POST | `/api/employees` | EmployeeDto | 201 + Location | utwórz employee |
| PUT | `/api/employees/{id}` | EmployeeDto | 204 | zaktualizuj employee |
| DELETE | `/api/employees/{id}` | - | 204 | usuń employee |

### BookController (`/api/books`)
| Metoda | URL | Body | Status | Opis |
|---|---|---|---|---|
| GET | `/api/books` | - | 200 | lista książek |
| GET | `/api/books/{id}` | - | 200/404 | szczegóły książki |
| POST | `/api/books` | BookDto | 201 + Location | utwórz książkę |
| PUT | `/api/books/{id}` | BookDto | 204 | pełna aktualizacja |
| PATCH | `/api/books/{id}` | BookDto (partial) | 204 | częściowa aktualizacja |
| DELETE | `/api/books/{id}` | - | 204 | usuń książkę |

---

## KLUCZOWE FRAGMENTY KODU (które mogą usunąć)

### 1. HATEOAS - metoda pomocnicza toModel()
```java
// CompanyController
private EntityModel<CompanyDto> toModel(Company company) {
    CompanyDto dto = modelMapper.map(company, CompanyDto.class);
    return EntityModel.of(dto,
        linkTo(methodOn(CompanyController.class).getCompanyById(company.getId())).withSelfRel(),
        linkTo(methodOn(CompanyController.class).getEmployeesOfCompany(company.getId())).withRel("employees")
    );
}

// EmployeeController
private EntityModel<EmployeeDto> toModel(Employee employee) {
    EmployeeDto dto = modelMapper.map(employee, EmployeeDto.class);
    List<Link> links = new ArrayList<>();
    links.add(linkTo(methodOn(EmployeeController.class).getEmployeeById(employee.getId())).withSelfRel());
    if (employee.getEmployer() != null) {
        links.add(linkTo(methodOn(CompanyController.class)
            .getCompanyById(employee.getEmployer().getId())).withRel("employer"));
    }
    return EntityModel.of(dto, links);
}
```

### 2. POST endpoint (tworzenie zasobu)
```java
@PostMapping
public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDto companyDto) {
    Company company = modelMapper.map(companyDto, Company.class);
    company.setId(null);  // wymuszamy generowanie ID przez JPA
    Company saved = companyRepository.save(company);
    URI location = linkTo(methodOn(CompanyController.class)
        .getCompanyById(saved.getId())).toUri();
    return ResponseEntity.created(location).build();
    // HTTP 201 Created z nagłówkiem Location: /api/companies/1
}
```

### 3. PUT endpoint (pełna aktualizacja)
```java
@PutMapping("/{companyId}")
public ResponseEntity<?> updateCompany(
        @PathVariable Long companyId,
        @Valid @RequestBody CompanyDto companyDto) {
    return companyRepository.findById(companyId)
        .map(existing -> {
            existing.setName(companyDto.getName());
            companyRepository.save(existing);
            return ResponseEntity.noContent().<Void>build();  // HTTP 204
        })
        .orElse(ResponseEntity.notFound().build());           // HTTP 404
}
```

### 4. PATCH endpoint (częściowa aktualizacja - tylko Book)
```java
@PatchMapping("/{bookId}")
public ResponseEntity<?> partialUpdateBook(
        @PathVariable Long bookId,
        @RequestBody BookDto bookDto) {  // BRAK @Valid - partial!
    return bookRepository.findById(bookId)
        .map(existing -> {
            if (bookDto.getTitle() != null) existing.setTitle(bookDto.getTitle());
            if (bookDto.getAuthor() != null) existing.setAuthor(bookDto.getAuthor());
            if (bookDto.getIsbn() != null) existing.setIsbn(bookDto.getIsbn());
            if (bookDto.getPublishedYear() != null) existing.setPublishedYear(bookDto.getPublishedYear());
            if (bookDto.getPrice() != null) existing.setPrice(bookDto.getPrice());
            bookRepository.save(existing);
            return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
}
```

### 5. Przypisanie Employee do Company (link)
```java
@PostMapping("/{companyId}/employees/{empId}")
public ResponseEntity<?> addEmployeeToCompany(
        @PathVariable Long companyId,
        @PathVariable Long empId) {
    Company company = companyRepository.findById(companyId)
        .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    Employee employee = employeeRepository.findById(empId)
        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    employee.setEmployer(company);   // ustawiamy relację po stronie właściciela (Employee)
    employeeRepository.save(employee);
    return ResponseEntity.noContent().build();
}
```

### 6. Usunięcie Company (z odłączeniem employees)
```java
@DeleteMapping("/{companyId}")
public ResponseEntity<?> deleteCompany(@PathVariable Long companyId) {
    return companyRepository.findById(companyId)
        .map(company -> {
            // odłącz wszystkich employees przed usunięciem firmy
            List<Employee> employees = employeeRepository.findByEmployer_Id(companyId);
            employees.forEach(e -> {
                e.setEmployer(null);
                employeeRepository.save(e);
            });
            companyRepository.delete(company);
            return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
}
```

### 7. GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.FieldErrorDetail> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new ValidationErrorResponse.FieldErrorDetail(
                fe.getField(), fe.getDefaultMessage()))
            .collect(Collectors.toList());

        return ResponseEntity.badRequest()
            .body(new ValidationErrorResponse(400, "Validation failed", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(new ValidationErrorResponse(400, ex.getMessage(), List.of()));
    }
}
```

### 8. DataInitializer (dane startowe)
```java
@Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (companyRepository.count() == 0) {  // idempotentne - nie duplikuje danych
            Company acme = companyRepository.save(Company.builder().name("Acme Corp").build());
            Company globex = companyRepository.save(Company.builder().name("Globex").build());
            employeeRepository.save(new Employee(null, "Jan", "Kowalski",
                LocalDate.of(1990, 5, 15), "Developer", acme));
            employeeRepository.save(new Employee(null, "Anna", "Nowak",
                LocalDate.of(1985, 8, 22), "Manager", acme));
            employeeRepository.save(new Employee(null, "Piotr", "Wiśniewski",
                LocalDate.of(1992, 3, 10), "Analyst", globex));
        }
    }
}
```

### 9. ModelMapper bean w Sri02Application
```java
@SpringBootApplication
public class Sri02Application {
    public static void main(String[] args) {
        SpringApplication.run(Sri02Application.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
        // @Bean - Spring rejestruje ten obiekt jako singleton w kontenerze IoC
        // Wstrzykiwany przez @RequiredArgsConstructor w controllerach
    }
}
```

---

## HATEOAS - JAK DZIAŁA

**EntityModel** = jeden zasób + linki
**CollectionModel** = kolekcja zasobów + linki

```java
// Jeden zasób
EntityModel.of(dto, Link.of(...).withSelfRel())

// Kolekcja
List<EntityModel<CompanyDto>> items = companies.stream()
    .map(this::toModel)
    .collect(Collectors.toList());
CollectionModel.of(items,
    linkTo(methodOn(CompanyController.class).getAllCompanies()).withSelfRel())

// Generowanie linku do endpointu
linkTo(methodOn(CompanyController.class).getCompanyById(id)).withSelfRel()
// → "http://localhost:8080/api/companies/1"
```

**JSON odpowiedź z HATEOAS:**
```json
{
  "id": 1,
  "name": "Acme Corp",
  "_links": {
    "self": { "href": "http://localhost:8080/api/companies/1" },
    "employees": { "href": "http://localhost:8080/api/companies/1/employees" }
  }
}
```

---

## LOMBOK - NAJWAŻNIEJSZE ADNOTACJE

| Adnotacja | Co generuje |
|---|---|
| `@Data` | gettery + settery + toString + equals + hashCode |
| `@NoArgsConstructor` | konstruktor bezargumentowy |
| `@AllArgsConstructor` | konstruktor ze wszystkimi polami |
| `@Builder` | wzorzec builder (`Company.builder().name("X").build()`) |
| `@RequiredArgsConstructor` | konstruktor z polami `final` (używany do DI) |
| `@ToString.Exclude` | wyklucza pole z toString() (zapobiega pętli przy relacjach) |
| `@EqualsAndHashCode.Exclude` | wyklucza pole z equals/hashCode |

---

## PYTANIA KTÓRE MOGĄ PAŚĆ

**Q: Dlaczego `@ToString.Exclude` na Employee w Company?**
A: Company.employees → Employee.employer → Company... → StackOverflowError. Exclude przerywa cykl.

**Q: Różnica PUT vs PATCH?**
A: PUT = pełna zamiana zasobu (wszystkie pola wymagane), PATCH = częściowa aktualizacja (tylko podane pola, brak `@Valid`).

**Q: Dlaczego `employee.setId(null)` przed save w POST?**
A: Gdyby klient przysłał `id` w JSON, JPA zrobiłby update zamiast insert. Null = wymuś wygenerowanie nowego ID.

**Q: Co to `mappedBy` w @OneToMany?**
A: Wskazuje, które pole po stronie "wielu" (Employee) zawiera klucz obcy. Właścicielem relacji jest zawsze strona z `@JoinColumn`.

**Q: Jak działa `findByEmployer_Id`?**
A: Spring Data parsuje nazwę: `findBy` + `Employer` (nawigacja do relacji) + `_Id` (pole id w Company). SQL: `WHERE employer_id = ?`

**Q: Różnica `@NotBlank` vs `@NotNull`?**
A: `@NotNull` → nie null. `@NotBlank` → nie null + nie pusty String + nie same spacje. Dla stringów zwykle chcemy `@NotBlank`.

**Q: Co to `@RestControllerAdvice`?**
A: Globalny handler wyjątków dla wszystkich `@RestController`. `@ExceptionHandler` w środku wyłapuje konkretny typ wyjątku.

**Q: Co to CommandLineRunner?**
A: Interfejs Spring Boot. Metoda `run()` jest wywoływana automatycznie po starcie aplikacji. Używamy do inicjalizacji danych.

**Q: Jaka różnica JpaRepository vs CrudRepository?**
A: `JpaRepository` rozszerza `CrudRepository` o paginację, sortowanie i `flush()`. CompanyRepository używa JPA (ma `findAll()` z paginacją), Employee/Book używają Crud (prostszy).

**Q: Jak działa ModelMapper?**
A: Automatycznie mapuje pola o tych samych nazwach między klasami. `modelMapper.map(entity, Dto.class)` → kopiuje pola. Zdefiniowany jako `@Bean` → singleton wstrzykiwany przez konstruktor.

**Q: Co to HATEOAS i po co?**
A: Hypermedia As The Engine Of Application State. Zasoby REST zawierają linki do powiązanych zasobów/akcji. Klient nie musi znać URL-i na sztywno – odkrywa je z odpowiedzi. Używamy `EntityModel`, `CollectionModel`, `linkTo(methodOn(...))`.

---

## DANE TESTOWE (po starcie aplikacji)

| ID | Firma |
|---|---|
| 1 | Acme Corp |
| 2 | Globex |

| ID | Imię | Nazwisko | Stanowisko | Firma |
|---|---|---|---|---|
| 1 | Jan | Kowalski | Developer | Acme Corp |
| 2 | Anna | Nowak | Manager | Acme Corp |
| 3 | Piotr | Wiśniewski | Analyst | Globex |

**H2 Console:** `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:sri-hr`
- User: `sa`, Password: (puste)
