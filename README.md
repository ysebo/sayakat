# Sayakat

A web platform for travelling in Kyrgyzstan: personalised multi-day route planning plus a small marketplace
for local guides, guesthouses and drivers.

Bachelor thesis, Computer Science, Ala-Too International University.

## Stack

- **api/** — Java 21, Spring Boot 3.5, Spring Security (JWT), PostgreSQL + PostGIS, Flyway, Timefold Solver
- **web/** — Vue 3, Vite, PrimeVue 4, Tailwind CSS 4, Leaflet, Pinia

## Getting started

```bash
docker compose up -d db
cd api && mvn spring-boot:run      # http://localhost:8080/swagger-ui.html
cd web && npm install && npm run dev   # http://localhost:5173
```

The `dev` profile seeds demo data on first start. Demo accounts: `tourist@sayakat.local`,
`provider@sayakat.local`, `admin@sayakat.local` with password `demo12345`.

## Route planner

The planner solves a multi-day orienteering problem with time windows and lodging selection:

- **hard:** opening hours, day length, seasons, pace, reachable lodging, budget
- **medium:** maximise the personalised value of visited places
- **soft:** minimise driving time

A greedy algorithm is included as a baseline. To compare it with Timefold:

```bash
cd api && mvn -Pexperiments test
```

Results are written to `api/target/experiments/`.

For real road travel times, see [infra/osrm](infra/osrm/README.md).
