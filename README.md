# SRI - Ćwiczenie 2 + 3: REST API

**Autor:** s34669
**Technologia:** Java 21, Spring Boot 3, H2 (baza in-memory), Spring HATEOAS

---

## Opis

Aplikacja udostępnia REST API do zarządzania pracownikami i firmami (relacja jeden-do-wielu) oraz książkami. Zaimplementowano pełen CRUD, walidację danych wejściowych, obsługę błędów oraz mechanizm HATEOAS.

---

## Model danych

### Company
| Pole | Typ | Opis |
|------|-----|------|
| id | Long | identyfikator (auto) |
| name | String | nazwa firmy |
| employees | Set\<Employee\> | pracownicy (OneToMany) |

### Employee
| Pole | Typ | Opis |
|------|-----|------|
| id | Long | identyfikator (auto) |
| firstName | String | imię |
| lastName | String | nazwisko |
| birthDate | LocalDate | data urodzenia |
| job | String | stanowisko |
| employer | Company | firma (ManyToOne) |

### Book
| Pole | Typ | Opis |
|------|-----|------|
| id | Long | identyfikator (auto) |
| title | String | tytuł |
| author | String | autor |
| isbn | String | numer ISBN |
| publishedYear | Integer | rok wydania |
| price | Double | cena |

---

## Endpointy

### Companies `/api/companies`

| Metoda | URL | Opis | Status |
|--------|-----|------|--------|
| GET | `/api/companies` | Lista firm | 200 |
| GET | `/api/companies/{id}` | Firma z pracownikami | 200 / 404 |
| POST | `/api/companies` | Dodaj firmę | 201 / 400 |
| PUT | `/api/companies/{id}` | Aktualizuj firmę | 204 / 400 / 404 |
| DELETE | `/api/companies/{id}` | Usuń firmę | 204 / 404 |
| GET | `/api/companies/{id}/employees` | Tylko pracownicy firmy | 200 / 404 |
| POST | `/api/companies/{cId}/employees/{eId}` | Przypisz pracownika do firmy | 204 / 404 |
| DELETE | `/api/companies/{cId}/employees/{eId}` | Odepnij pracownika od firmy | 204 / 404 |

### Employees `/api/employees`

| Metoda | URL | Opis | Status |
|--------|-----|------|--------|
| GET | `/api/employees` | Lista pracowników | 200 |
| GET | `/api/employees/{id}` | Pracownik po ID | 200 / 404 |
| POST | `/api/employees` | Dodaj pracownika | 201 / 400 |
| PUT | `/api/employees/{id}` | Aktualizuj pracownika | 204 / 400 / 404 |
| DELETE | `/api/employees/{id}` | Usuń pracownika | 204 / 404 |

### Books `/api/books`

| Metoda | URL | Opis | Status |
|--------|-----|------|--------|
| GET | `/api/books` | Lista książek | 200 |
| GET | `/api/books/{id}` | Książka po ID | 200 / 404 |
| POST | `/api/books` | Dodaj książkę | 201 / 400 |
| PUT | `/api/books/{id}` | Aktualizuj książkę | 204 / 400 / 404 |
| PATCH | `/api/books/{id}` | Częściowa aktualizacja | 204 / 404 |
| DELETE | `/api/books/{id}` | Usuń książkę | 204 / 404 |

---

## Walidacja

Błędy walidacji zwracają status `400` z ciałem odpowiedzi w formacie:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    { "field": "name", "message": "Company name is required" },
    { "field": "name", "message": "Name must be between 2 and 200 characters" }
  ]
}
```

---

## HATEOAS

Każdy zasób zawiera linki `_links`. Przykład odpowiedzi dla firmy:

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

## Uruchomienie

```bash
./mvnw spring-boot:run
```

Aplikacja dostępna pod: `http://localhost:8080`
Konsola H2: `http://localhost:8080/h2-console`

Przy starcie aplikacji `DataInitializer` automatycznie tworzy przykładowe dane (2 firmy, 3 pracowników).

---

## Testowanie

Kolekcja Postmana: `sri02-sri03.postman_collection.json`

Zawiera żądania testujące:
- pełny CRUD dla Company, Employee, Book
- zarządzanie relacjami (przypisanie / odpięcie pracownika)
- walidację (żądania z niepoprawnymi danymi, oczekiwany status 400)
- linki HATEOAS w odpowiedziach
