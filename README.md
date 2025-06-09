# 📄 Invoice Management API

A comprehensive, production-grade RESTful API for managing invoices, built with Spring Boot. This project supports full CRUD operations, advanced search, filtering, reporting, and export capabilities—ideal for showcasing backend engineering skills in your portfolio.

<img src='./screenshot%20.png' title='screenshot'  alt='screenshot'/>

## 🚀 Features

### 🔧 Invoice Management

* Create, retrieve, update, and delete invoices
* Search by:

  * Invoice number
  * Status
  * Customer email
  * Total amount (greater than / less than)
  * Due date range
  * Client name (partial match)
* Advanced multi-criteria search (client name, status, date, amount)
* Generate new invoice numbers
* Paginated results for all list endpoints
* Controlled state transitions and validation

### 📊 Reports & Exports

* Export invoices in **PDF**, **CSV**, and **Excel** formats
* Export:

  * Single invoice
  * Bulk export (by IDs or by criteria)
* Revenue reports:

  * By customer (within date range)
  * By month (year-wise)
* Accounts aging report (overdue analysis)
* Invoices by status report
* All reports exportable in multiple formats

---

## 🛠️ Tech Stack

| Layer      | Technology                   |
| ---------- | ---------------------------- |
| Language   | Java 17                      |
| Framework  | Spring Boot 3                |
| REST Docs  | Swagger / OpenAPI            |
| Build Tool | Maven or Gradle              |
| Pagination | Spring Data Pageable         |
| Export     | Apache POI / iText |
| Logging    | SLF4J + Logback              |

---

## 📦 API Endpoints

### Invoice Controller `/api/invoices`

| Method | Endpoint                      | Description                    |
| ------ | ----------------------------- | ------------------------------ |
| POST   | `/`                           | Create a new invoice           |
| GET    | `/{id}`                       | Get invoice by ID              |
| GET    | `/number/{invoiceNumber}`     | Get invoice by invoice number  |
| GET    | `/`                           | Get all invoices (paginated)   |
| PUT    | `/{id}`                       | Update an invoice              |
| PATCH  | `/{id}/status`                | Update invoice status          |
| DELETE | `/{id}`                       | Delete an invoice              |
| GET    | `/status/{status}`            | Get invoices by status         |
| GET    | `/amount-greater/{amount}`    | Get invoices ≥ given amount    |
| GET    | `/amount-less/{amount}`       | Get invoices ≤ given amount    |
| GET    | `/customer/{email}`           | Get invoices by customer email |
| GET    | `/due-date?startDate&endDate` | Get invoices by due date range |
| GET    | `/overdue`                    | Get overdue invoices           |
| GET    | `/search`                     | Advanced invoice search        |
| GET    | `/generate-number`            | Generate a new invoice number  |

### Report Controller `/api/reports`

| Method | Endpoint                             | Description                        |
| ------ | ------------------------------------ | ---------------------------------- |
| GET    | `/invoice/{id}/export`               | Export single invoice              |
| POST   | `/invoices/export`                   | Export multiple invoices           |
| GET    | `/invoices/export-by-criteria`       | Export invoices by search criteria |
| GET    | `/revenue/by-customer`               | Revenue report by customer         |
| GET    | `/revenue/by-customer/export`        | Export revenue by customer         |
| GET    | `/revenue/by-month?year=YYYY`        | Revenue report by month            |
| GET    | `/revenue/by-month/export?year=YYYY` | Export revenue by month            |
| GET    | `/aging`                             | Accounts receivable aging report   |
| GET    | `/aging/export`                      | Export aging report                |
| GET    | `/status`                            | Invoices by status report          |
| GET    | `/status/export`                     | Export invoices by status report   |

---

## 🧪 Running Locally

```bash
# Clone repository
git clone https://github.com/your-username/invoice-api.git
cd invoice-api

# Build and run
./mvnw spring-boot:run

# Access API documentation
http://localhost:8080/swagger-ui.html
```

---

## 📚 Swagger / OpenAPI

All endpoints are fully documented using OpenAPI annotations and viewable via Swagger UI.

---

## ✅ Validation & Error Handling

* Proper HTTP status codes (200, 201, 400, 404, 409, 204)
* Input validation via annotations (`@Valid`)
* Business logic validation (e.g., final state cannot be edited)

---

## 📂 Folder Structure (Overview)

```
src/main/java
└── com.example.invoiceapi
    ├── controller         # REST controllers
    ├── service            # Business logic
    ├── dto                # Data transfer objects
    ├── model              # Entity and enums
    ├── repository         # Spring Data interfaces
    ├── export             # Export utilities (PDF/CSV/Excel)
    └── config             # Swagger, logging, etc.
```

