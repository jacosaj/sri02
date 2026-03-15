# SRI - Ćwiczenie 2: REST API

**Autor:** s34669
**Technologia:** Java 21, Spring Boot 3, H2 (baza in-memory)

---

## Opis

Aplikacja udostępnia REST API do zarządzania kolekcją książek. Zaimplementowano pełen zestaw operacji CRUD z użyciem Spring Boot oraz Spring Data JPA.

---

## Model danych – Book

| Pole | Typ | Opis |
|------|-----|------|
| id | Long | identyfikator (auto) |
| title | String | tytuł książki |
| author | String | autor |
| isbn | String | numer ISBN |
| publishedYear | Integer | rok wydania |
| price | Double | cena |

---

## Endpointy

| Metoda | URL | Opis | Status |
|--------|-----|------|--------|
| GET | `/api/books` | Pobierz wszystkie książki | 200 |
| GET | `/api/books/{id}` | Pobierz książkę po ID | 200 / 404 |
| POST | `/api/books` | Dodaj nową książkę | 201 |
| PUT | `/api/books/{id}` | Zaktualizuj całą książkę | 204 / 404 |
| PATCH | `/api/books/{id}` | Częściowa aktualizacja | 204 / 404 |
| DELETE | `/api/books/{id}` | Usuń książkę | 204 / 404 |

---

## Uruchomienie

```bash
./mvnw spring-boot:run
```

Aplikacja dostępna pod: `http://localhost:8080`
Konsola H2: `http://localhost:8080/h2-console`

---

## Testowanie

Kolekcja Postman znajduje się w katalogu `postman/`.
Należy ją zaimportować do Postmana i ustawić zmienne kolekcji:

- `baseUrl` – domyślnie `http://localhost:8080`
- `bookId` – ID książki do operacji na pojedynczym obiekcie
