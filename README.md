Car Rental System

A full-stack car rental application built with Java 21, Spring Boot,
React, TypeScript, and PostgreSQL.

Features
- Check availability for Sedans, SUVs, and Vans.
- Create and view reservations.
- Cancel reservations.
- Prevent overlapping bookings.
- Handle concurrent reservation requests.

Tech Stack
- Backend: Java 21, Spring Boot, Spring Data JPA
- Frontend: React, TypeScript, Vite
- Database: PostgreSQL
- Migrations: Flyway
- Testing: JUnit 5, Mockito, Spring Boot Test
- Environment: Docker Compose

Run Locally

1. Start PostgreSQL
cd infrastructure
docker compose up -d

2. Start the backend
cd backend/car-rental-api
./mvnw spring-boot:run
Backend: http://localhost:8080
3. Start the frontend
In a separate terminal:
cd frontend/car-rental-web
npm install
npm run dev
Frontend: http://localhost:5173

API Endpoints

  Method   Endpoint                   Description
  GET      /api/cars/availability   Check vehicle availability
POST     /api/reservations        Create a reservation
GET      /api/reservations/{id}   Get reservation details
DELETE   /api/reservations/{id}   Cancel a reservation

Reservation Rules
- Rental duration must be at least one day.
- A vehicle cannot have overlapping confirmed reservations.
- Back-to-back reservations are allowed.
- Cancelled reservations remain in the database for history.

Testing

Run all backend tests:
cd backend/car-rental-api
./mvnw clean verify
Latest verified result: 21 tests passed, with 0 failures and 0
errors.
Project Structure
car-rental-system/
├── backend/
├── frontend/
├── infrastructure/
└── README.md

Design Highlights

- Layered backend architecture: controllers, services, repositories,
  and entities.
- Flyway database migrations.
- Transactional reservation allocation with pessimistic locking.
- Automated unit, integration, and concurrency tests.
Future Improvements
- Customer authentication
- Pricing and payment integration
- Reservation updates
- Deployment and monitoring