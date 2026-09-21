# WolnyTermin

System rezerwacji terminów online. Założenia produktowe i architektoniczne: [ZALOZENIA.md](ZALOZENIA.md).

## Wymagania

- JDK 21
- Maven 3.9+ (albo wbudowany w IntelliJ)
- Docker (do uruchomienia PostgreSQL)

## Uruchomienie

1. Baza danych:

   ```bash
   docker compose up -d
   ```

   PostgreSQL 16 wstaje na `localhost:5432`, baza/użytkownik/hasło: `wolnytermin`.
   Dane przeżywają restart kontenera (wolumen `wolnytermin-pgdata`).

2. Aplikacja:

   ```bash
   mvn spring-boot:run
   ```

   Przy starcie Flyway zakłada schemat (migracje w `src/main/resources/db/migration`).

3. Sprawdzenie:

   ```bash
   curl http://localhost:8080/api/ping
   curl http://localhost:8080/actuator/health
   ```

Konfigurację bazy można nadpisać zmiennymi `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Testy

```bash
mvn test
```

## Zasady pracy ze schematem

- Schemat zmienia **wyłącznie** Flyway — nowy plik `V2__opis.sql`, `V3__...` itd.
- Raz zmigrowanego pliku się nie edytuje (Flyway pilnuje sum kontrolnych).
- Hibernate ma `ddl-auto: validate` — nie tworzy i nie modyfikuje tabel, tylko sprawdza
  zgodność encji ze schematem.

## Struktura

```
src/main/java/pl/wolnytermin/
├── WolnyTerminApplication.java   punkt wejścia
├── config/                       konfiguracja (Clock, strefa czasowa)
└── common/                       wspólne elementy (na razie endpoint /api/ping)
```

Kolejne moduły domenowe (`company`, `employee`, `service`, `availability`, `booking`)
dochodzą jako osobne pakiety.
