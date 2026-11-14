# ifs25024-p5-pbo2026-springboot-cashflow

Aplikasi catatan keuangan (Cashflow) — Praktikum PBO 2026 P5 (Spring Boot 4.1.2, Vaadin 25.3.4, JDBC PostgreSQL).

## Menjalankan

1. Buat database: `CREATE DATABASE pbo2026_cashflow;`
2. Salin `.env.example` menjadi `.env`, lalu isi `DB_PASSWORD` (dan `SESSION_SECRET`).
3. Jalankan:

```powershell
.\mvnw.cmd spring-boot:run
```

Buka http://localhost:8080. Login awal: `ROOT_USERNAME` / `ROOT_PASSWORD` dari `.env` (default `admin` / `admin`).

## Build dan test

```powershell
.\mvnw.cmd clean verify                 # test + Jacoco 100% (view/auth dikecualikan)
.\mvnw.cmd clean package -Pproduction   # JAR produksi
java -jar target\ifs25024-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Unit test memakai repository in-memory, test JDBC dan `AppTest` memakai H2 (`src/test/resources/application-test.yaml`).

## Struktur

`domain` ← `usecase` ← `adapter` / `framework`; wiring di `config/AppConfig`.

| Rute | Akses |
| --- | --- |
| `/login`, `/register` | publik |
| `/`, `/profile` | USER dan ADMIN |
| `/users`, `/user-transactions?userId=N` | ADMIN |
| `/uploads/**` | publik (foto profil dan bukti) |
