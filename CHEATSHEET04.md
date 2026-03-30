# Cheatsheet - SRI ćwiczenia 4: Spring Security + JWT

---

## Co robi projekt

REST API zabezpieczone tokenami JWT. Trzy poziomy dostępu do endpointów pacjentów:
- publiczny (bez logowania)
- dla zalogowanych
- tylko dla admina

Użytkownicy są w bazie H2, hasła przechowywane jako hasze BCrypt.

---

## Przepływ logowania

1. Klient wysyła `POST /api/auth/login` z emailem i hasłem
2. `AuthenticationManager` wywołuje `UserService.loadUserByUsername()`
3. `UserService` pobiera użytkownika z bazy przez `AppUserRepository`
4. Spring porównuje hasło z hashem BCrypt
5. Jeśli OK → `JwtIssuer` tworzy i podpisuje token
6. Klient dostaje `{ "token": "eyJ..." }`

## Przepływ żądania z tokenem

1. Klient wysyła nagłówek `Authorization: Bearer eyJ...`
2. `JwtAuthenticationFilter` wyciąga token (usuwa prefiks "Bearer ")
3. `JwtDecoder` weryfikuje podpis HMAC256 i datę wygaśnięcia
4. Z claimów tokenu budowany jest `UserPrincipal`
5. `UserPrincipalAuthenticationToken` trafia do `SecurityContextHolder`
6. Spring Security sprawdza uprawnienia i wpuszcza żądanie

---

## Endpointy

| Metoda | URL | Dostęp | Kod |
|--------|-----|--------|-----|
| POST | `/api/auth/login` | wszyscy | `AuthController` |
| GET | `/api/patients` | wszyscy | `PatientController` |
| GET | `/api/patients/{id}` | zalogowani | `PatientController` |
| POST | `/api/patients` | ROLE_ADMIN | `PatientController` |
| PUT | `/api/patients/{id}` | ROLE_ADMIN | `PatientController` |
| DELETE | `/api/patients/{id}` | ROLE_ADMIN | `PatientController` |

## Konta testowe

| Email | Hasło | Rola |
|-------|-------|------|
| admin@pandas.pl | admin123 | ROLE_ADMIN |
| user@pandas.pl | user123 | ROLE_USER |

---

## Kluczowe klasy i ich rola

### AppUser
Encja JPA w bazie danych. Przechowuje email, hash hasła i rolę.
Nie jest używana bezpośrednio przez Spring Security.

### UserPrincipal
Implementuje interfejs `UserDetails` z Spring Security.
Reprezentuje zalogowanego użytkownika w kontekście bezpieczeństwa.
Nie jest encją ani DTO — służy tylko wewnętrznie.

### UserService
Implementuje `UserDetailsService`. Metoda `loadUserByUsername(email)`
pobiera `AppUser` z bazy i mapuje go na `UserPrincipal`.
Wywoływana automatycznie przez `AuthenticationManager` przy logowaniu.

### JwtProperties
Czyta `security.jwt.secret-key` z `application.properties`.
Klucz jest potrzebny do podpisywania i weryfikacji tokenu.

### JwtIssuer
Tworzy token JWT przy logowaniu. Token zawiera:
- `sub` — id użytkownika
- `email` — email użytkownika
- `roles` — lista ról (np. ["ROLE_ADMIN"])
- `exp` — czas wygaśnięcia (60 minut od wystawienia)

Podpisywany algorytmem HMAC256 z tajnym kluczem.

### JwtDecoder
Weryfikuje token przy każdym żądaniu:
- sprawdza podpis HMAC256
- sprawdza czy token nie wygasł
- odczytuje claims i buduje `UserPrincipal`

### UserPrincipalAuthenticationToken
Rozszerza `AbstractAuthenticationToken`. To jest "zaświadczenie"
dla Spring Security że użytkownik jest uwierzytelniony.
Nie należy mylić z tokenem JWT — to wewnętrzny mechanizm Springa.

### JwtAuthenticationFilter
Rozszerza `OncePerRequestFilter` — wykonuje się raz na każde żądanie HTTP.
Wyciąga token z nagłówka, dekoduje go i ustawia kontekst bezpieczeństwa.
Jeśli token jest nieważny lub brak tokenu — żądanie trafia dalej bez uwierzytelnienia
(Spring Security sam wtedy zwróci 401 dla chronionych endpointów).

### WebSecurityConfig
Główna konfiguracja bezpieczeństwa:
- wyłączona obsługa CORS i CSRF
- brak sesji po stronie serwera (`STATELESS`) — REST jest bezstanowy
- wyłączony formularz logowania
- reguły dostępu: login i GET pacjentów publiczne, reszta wymaga tokenu
- filtr JWT dodany przed `UsernamePasswordAuthenticationFilter`

### SecurityBeans
Dostarcza bean `PasswordEncoder` (delegujący do BCrypt).
Używany przez `AuthenticationManager` do porównania hasła oraz przez
`DataInitializer` do hashowania haseł przy inicjalizacji danych.

---

## Dlaczego STATELESS

REST jest bezstanowy z założenia. Serwer nie przechowuje sesji użytkownika.
Każde żądanie musi samo w sobie zawierać informację o tożsamości (token JWT).
Dzięki temu aplikacja jest łatwiejsza do skalowania.

## Dlaczego JWT a nie sesje

- Token jest podpisany — serwer weryfikuje podpis bez potrzeby zapytania do bazy
- Token zawiera wszystkie potrzebne dane (email, role)
- Działa dobrze w architekturach rozproszonych

## Dlaczego hasze haseł

Gdyby ktoś uzyskał dostęp do bazy danych, nie zobaczyłby haseł w jawnej postaci.
BCrypt jest wolnym algorytmem co utrudnia ataki brute-force.
Prefiks `{bcrypt}` w hashu informuje Spring który algorytm zastosować.

## Dlaczego ROLE_ prefiks

Spring Security rozróżnia role od innych uprawnień (tzw. authorities).
`hasRole('ADMIN')` automatycznie szuka `ROLE_ADMIN` w kolekcji authorities.
Bez prefiksu trzeba używać `hasAuthority('ADMIN')`.

---

## Odpowiedzi HTTP

| Sytuacja | Kod |
|----------|-----|
| Sukces logowania | 200 OK + token |
| Złe hasło / brak użytkownika | 401 Unauthorized |
| Brak tokenu w żądaniu | 401 Unauthorized |
| Token ważny, brak roli | 403 Forbidden |
| Zasób nie istnieje | 404 Not Found |
| Błąd walidacji | 400 Bad Request |

---

## Struktura tokenu JWT

Token składa się z trzech części oddzielonych kropką: `header.payload.signature`

**Header** (zakodowany Base64):
```json
{ "alg": "HS256", "typ": "JWT" }
```

**Payload** (zakodowany Base64):
```json
{
  "sub": "1",
  "exp": 1234567890,
  "email": "admin@pandas.pl",
  "roles": ["ROLE_ADMIN"]
}
```

**Signature**: HMAC256(header + "." + payload, secretKey)

Payload **nie jest zaszyfrowany** — tylko zakodowany. Dlatego nie wstawiamy tam hasła ani wrażliwych danych.

---

## Najważniejsze pytania na obronie

**Czym różni się uwierzytelnienie od autoryzacji?**
Uwierzytelnienie (authentication) = potwierdzenie tożsamości (kto jesteś).
Autoryzacja (authorization) = sprawdzenie uprawnień (co możesz zrobić).

**Co to jest CSRF i dlaczego wyłączyliśmy ochronę?**
Cross-Site Request Forgery — atak polegający na wysyłaniu żądań w imieniu zalogowanego użytkownika.
Przy tokenach JWT przesyłanych w nagłówku (nie w cookies) CSRF nie jest zagrożeniem,
bo atakująca strona nie ma dostępu do nagłówków Authorization.

**Co się stanie gdy token wygaśnie?**
`JwtDecoder` rzuci wyjątek `JWTVerificationException`, filtr nie ustawi kontekstu,
Spring Security zwróci 401. Użytkownik musi się zalogować ponownie.

**Gdzie jest przechowywany sekret do podpisywania?**
W `application.properties` jako `security.jwt.secret-key`, odczytywany przez `JwtProperties`.
W produkcji powinien być w zmiennej środowiskowej, nie w repozytorium.

**Dlaczego `OncePerRequestFilter` a nie zwykły filtr?**
Gwarantuje że filtr wykona się dokładnie raz na żądanie HTTP,
nawet jeśli żądanie jest przekazywane między servletami wewnętrznie.
