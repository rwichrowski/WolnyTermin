# WolnyTermin — założenia projektu

System do rezerwacji terminów online (fryzjer, barber, docelowo inne usługi) — odpowiednik Booksy.

## Cel MVP

Umożliwić klientowi znalezienie usługodawcy i zarezerwowanie wizyty online, a usługodawcy — zarządzanie grafikiem, usługami i rezerwacjami bez telefonów i zeszytu.

## Zakres funkcjonalny MVP

### Klient
- Przegląda listę/wyszukiwarkę usługodawców (kategoria, lokalizacja)
- Widzi profil firmy: usługi, ceny, czas trwania, dostępne terminy
- Rezerwuje termin (bez płatności z góry na start)
- Dostaje potwierdzenie e-mail, może odwołać rezerwację

### Usługodawca (firma)
- Rejestracja firmy: nazwa, adres, godziny otwarcia, kategorie usług
- Zarządzanie listą pracowników (jeśli więcej niż jeden)
- Definiowanie usług: nazwa, cena, czas trwania, przypisany pracownik
- Grafik pracy: godziny, przerwy, urlopy/blokady terminów
- Panel: lista rezerwacji, możliwość anulowania/przesunięcia

### Poza MVP (v2+)
- Płatności online (przedpłaty, zaliczki)
- SMS-y (koszt per wiadomość — do decyzji biznesowej)
- Opinie i oceny
- Program lojalnościowy, marketplace produktów
- Aplikacja mobilna natywna

## Architektura — stos JVM

**Backend:** Java 21 (LTS) + Spring Boot 3.x
- Spring Web (REST API)
- Spring Data JPA / Hibernate
- Spring Security + JWT (autoryzacja klient/firma/admin)
- Spring Validation
- Flyway — migracje bazy danych (wersjonowanie schematu, ważne od pierwszego dnia)
- Maven lub Gradle (do wyboru, oba OK)

**Baza danych:** PostgreSQL
- Najbardziej naturalny wybór w ekosystemie JVM/Spring, dobre wsparcie dla transakcyjności potrzebnej przy rezerwacjach (unikanie double-bookingu)
- Alternatywa: SQL Server, jeśli wolisz zostać przy znanym silniku — Hibernate obsługuje oba bez większej różnicy w kodzie

**Frontend:** osobna aplikacja SPA
- React lub Vue — komunikacja z backendem przez REST API
- Ewentualnie na sam start: Thymeleaf (server-side rendering w Spring) dla szybszego MVP bez oddzielnego frontend builda — do rozważenia, minus: gorsze UX przy interaktywnym kalendarzu

**Hosting (start):** VPS + Docker Compose (aplikacja + PostgreSQL + reverse proxy np. Caddy/Nginx) — tanio i pod kontrolą. Później ewentualnie migracja do chmury.

## Kluczowe komponenty domenowe

- **Availability Engine** — liczenie wolnych slotów: godziny pracy − istniejące rezerwacje − blokady − bufor między wizytami. Najbardziej złożona logika w systemie, warto pokryć testami jednostkowymi od początku.
- **Booking Service** — tworzenie rezerwacji z ochroną przed race condition (dwóch klientów rezerwuje ten sam slot): transakcja + unikalny constraint w bazie (np. na `employee_id + start_time`) albo optimistic locking (`@Version` w JPA).
- **Notification Service** — wysyłka e-mail asynchronicznie (np. `@Async` + kolejka w pamięci na start, docelowo RabbitMQ jeśli ruch wzrośnie), żeby nie blokować requestu rezerwacji.
- **Multi-tenancy** — każda firma to tenant; na MVP wystarczy `company_id` jako kolumna w tabelach (shared database/schema), pełna izolacja to przerost formy na start.

## Szkic modelu danych (do rozwinięcia)

- `company` (firma, dane adresowe, godziny otwarcia)
- `employee` (pracownik, powiązany z firmą)
- `service` (usługa: nazwa, cena, czas trwania, firma)
- `working_hours` / `time_off` (dostępność pracownika)
- `booking` (rezerwacja: klient, usługa, pracownik, start/koniec, status)
- `customer` (dane klienta, może być bez pełnej rejestracji — rezerwacja "jako gość")

## Otwarte pytania do rozstrzygnięcia później

- Czy klient musi się rejestrować, czy dopuszczamy rezerwację jako gość (e-mail + telefon)?
- Jedna firma = jeden konto właściciela, czy od razu role (właściciel/pracownik) w MVP?
- Strefa czasowa — zakładam na start jedną (Europe/Warsaw), bez wsparcia wielostrefowego.
- Format nazwy domeny/produktu — na razie roboczo „WolnyTermin”.
