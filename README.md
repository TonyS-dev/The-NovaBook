
# EcoFleet - Vehicle Rental Management System

A robust desktop application for managing vehicle rentals, built with JavaFX, PostgreSQL, JDBC, and a professional exception handling system.

---

## 🖥️ Main Features

- **User Management:** Secure authentication, registration, and admin user bootstrap
- **Vehicle Management:** CRUD for cars and bicycles
- **Rental System:** Start, complete, and track rentals with cost calculation
- **Reporting:** Real-time analytics, rental history, and usage reports
- **Exception Handling:** Centralized, enum-based error codes and severity
- **Modern UI:** Responsive JavaFX interface

---

## 🛠️ Tech Stack

- **Java 17** (core language)
- **JavaFX 21** (UI)
- **PostgreSQL 15** (database, via Docker)
- **JDBC** (database connectivity)
- **jBCrypt** (password hashing)
- **Maven** (build/dependency management)
- **Docker Compose** (database containerization)

---

## 📁 Project Structure

- `src/main/java/com/codeup/novabook/` — Main app, domain models, services, UI, utilities, exception hierarchy, infrastructure
- `src/main/resources/` — App configuration, sample data
- `db/migrations/` — SQL migration scripts (auto-applied on first run)
- `scripts/` — Project automation scripts (`init-project.sh`, `stop-project.sh`)
- `logs/` — Application logs
- `target/` — Build artifacts, JARs, Javadoc
- `EXCEPTION_HANDLING_GUIDE.md` — Exception system documentation
- `JAVADOC_DOCUMENTATION.md` — API documentation overview

---

## 🚀 Getting Started

1. **Copy `.env.example` to `.env` and set credentials**
2. **Run the startup script:**  
   `./scripts/init-project.sh`  
   (Starts PostgreSQL via Docker, waits for DB, builds with Maven, launches the app, logs to `logs/app.log`)
3. **Manual steps:**  
   - Build: `mvn clean package`
   - Run: `mvn javafx:run`

---


## 🌱 Configuration

- **Database:** PostgreSQL 15 in Docker, schema is automatically migrated from `db/migrations/init.sql`
- **App Config:** Set DB credentials in `.env` and `src/main/resources/application.properties`
- **Default Admin:** Created if no users exist (`admin` / `admin123`)
- **Sample Data:** See `src/main/resources/data/sample_data.sql`

---


## 📚 Documentation

- **Exception Handling:** See `EXCEPTION_HANDLING_GUIDE.md`
- **Javadoc:**
  - Automatically generated when building the project with Maven.
  - To generate and open the Javadoc in your browser, run:
    ```bash
    ./scripts/gen-javadoc.sh
    ```
  - This will build the Javadoc and open `target/site/apidocs/index.html` in a new browser tab (Linux/macOS/Windows supported).
  - See also `JAVADOC_DOCUMENTATION.md` for an overview of the API and documentation structure.---


## 👨‍💻 Author

- **TonyS-dev / Antonio Santiago**  
   [GitHub](https://github.com/TonyS-dev)  
   santiagor.acarlos@gmail.com

---
