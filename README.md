# SpendWise - Expense and Budget Tracker

Phase 1 implementation of SpendWise application with Spring Boot backend and React frontend.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- MySQL 8.x

## Database Setup

1. Install MySQL 8.x
2. Create the database:
`sql
CREATE DATABASE spendwise_db;
`

## Environment Variables

### Windows (Command Prompt)
`cmd
set DB_USERNAME=your_mysql_username
set DB_PASSWORD=your_mysql_password
`

### Windows (PowerShell)
`powershell
="your_mysql_username"
="your_mysql_password"
`

### Linux/Mac
`ash
export DB_USERNAME=your_mysql_username
export DB_PASSWORD=your_mysql_password
`

## Backend Setup

Additional optional environment variables for authentication (Phase 4):
- `JWT_SECRET` - HMAC signing secret, at least 32 bytes (defaults to an insecure dev value; set in production)
- `JWT_EXPIRATION_MS` - token lifetime in ms (defaults to 86400000 / 24h)
- `CORS_ALLOWED_ORIGINS` - comma-separated allowed frontend origins (defaults to `http://localhost:5173,http://localhost:3000`)


Navigate to backend directory:
`ash
cd backend
`

Compile and test:
`ash
./mvnw compile
./mvnw test
`

Run the application:
`ash
./mvnw spring-boot:run
`

Backend will run on http://localhost:8080

## Frontend Setup

Navigate to frontend directory:
`ash
cd frontend
`

Install dependencies:
`ash
npm install
`

Run development server:
`ash
npm run dev
`

Frontend will run on http://localhost:5173

Build for production:
`ash
npm run build
`

## Frontend Overview (Phase 6)

The React SPA consumes the backend at `VITE_API_BASE_URL` (see `frontend/.env`, default `http://localhost:8080/api/v1`). Authentication uses a JWT stored in `localStorage` and attached to requests by an Axios interceptor; a `401` response clears the session and redirects to login.

Routes:

- `/` - public landing page
- `/login`, `/register` - public auth pages (redirect to dashboard when already signed in)
- `/dashboard` - income/expense/balance summary and Recharts trend + category charts
- `/transactions` - paginated, filterable transaction CRUD
- `/categories` - income/expense category management (shared categories are read-only)
- `/budgets` - monthly budgets with backend-computed spent/remaining and warning status
- `/goals` - savings goals with progress updates
- `/reports` - monthly and yearly reports with category breakdowns

All monetary values are formatted in INR (en-IN). Run `npm run lint` to lint and `npm run build` for a production build.

## Testing (Phase 7)

Backend integration tests (H2, no external database required):

```bash
cd backend
./mvnw clean test
```

Covers auth, resource CRUD and filtering, ownership isolation (cross-user 403), server-computed budget/goal indicators, dashboard/report analytics, error-status contract, a real HTTP workflow on a random port, and JWT expiry — 39 tests.

Browser end-to-end tests (Playwright, drives the installed Google Chrome — no browser download). Start both services first, then run:

```bash
# terminal 1
cd backend && ./mvnw spring-boot:run

# terminal 2
cd frontend && npm run dev

# terminal 3
cd frontend && npx playwright test
```


## Phase 1 Status

- [x] Backend Spring Boot project created (Java 21, Spring Boot 4.0.0)
- [x] Required dependencies added (Spring Web MVC, Spring Data JPA, MySQL Connector/J, Validation)
- [x] Application properties configured with environment variables
- [x] Frontend React + Vite project created
- [x] Axios and React Router installed
- [x] Frontend environment variable configured (VITE_API_BASE_URL)
- [x] .gitignore configured
- [x] Basic application entry points created
- [x] Backend tests passing (mvn test)
- [x] Frontend builds successfully (npm run build)

## Phase Status

- Phase 1: COMPLETED - See [PHASE-1-REPORT.md](./PHASE-1-REPORT.md)

- Phase 2: COMPLETED - See [PHASE-2-REPORT.md](./PHASE-2-REPORT.md)

- Phase 3: COMPLETED - See [PHASE-3-REPORT.md](./PHASE-3-REPORT.md)

- Phase 4: COMPLETED - See [PHASE-4-REPORT.md](./PHASE-4-REPORT.md)

- Phase 5: COMPLETED - See [PHASE-5-REPORT.md](./PHASE-5-REPORT.md)

- Phase 6: COMPLETED - See [PHASE-6-REPORT.md](./PHASE-6-REPORT.md)

- Phase 7: COMPLETED - See [PHASE-7-REPORT.md](./PHASE-7-REPORT.md)
