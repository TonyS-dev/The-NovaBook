# 📚 NovaBook - Library Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?logo=java)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.9+-red?logo=apache-maven)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![License](https://img.shields.io/badge/License-MIT-green)

**A comprehensive, production-ready desktop library management system with modern architecture and professional exception handling.**

[Features](#-features) • [Architecture](#-architecture) • [Installation](#-installation) • [Usage](#-usage) • [Documentation](#-documentation)

</div>

---

## 🎯 Overview

**NovaBook** is a professional library management system designed for modern libraries. Built with JavaFX and PostgreSQL, it provides a robust, scalable solution for managing books, members, loans, and library operations with enterprise-grade error handling and data validation.

### 🌟 Key Highlights

- ✅ **Comprehensive Loan Management** - Track loans, returns, extensions, and overdue items with automatic fine calculation
- ✅ **Advanced Book Cataloging** - ISBN validation, reference pricing, multi-copy management, and CSV import/export
- ✅ **Member Management** - Registration, suspension tracking, and eligibility validation
- ✅ **Business Rules Enforcement** - Maximum 3 active loans per member, duplicate loan prevention, overdue restrictions
- ✅ **Enterprise Exception Handling** - Centralized error management with severity levels and error codes
- ✅ **Modern UI/UX** - Responsive JavaFX interface with real-time data refresh
- ✅ **Production-Ready** - Comprehensive logging, transaction management, and data integrity

---

## 🖥️ Features

### 📖 Book Management
- **CRUD Operations** - Create, read, update, and deactivate books
- **ISBN Validation** - Automatic validation with checksum verification (ISBN-10/13)
- **Reference Pricing** - Track book values for inventory management
- **Copy Management** - Track total and available copies per book
- **CSV Import/Export** - Bulk operations with detailed error reporting
- **Active/Inactive Filter** - Easy catalog organization

### 👥 Member Management
- **Member Registration** - Complete profile management with validation
- **Status Tracking** - Active, Suspended, Inactive statuses
- **Document Validation** - Unique document ID enforcement
- **Eligibility Checking** - Automatic validation for loan operations
- **Contact Management** - Email, phone, and address tracking

### 📚 Loan Operations
- **Create Loans** - Intelligent validation with business rule enforcement
- **Return Processing** - Automatic fine calculation for overdue items
- **Loan Extensions** - Extend due dates with validation
- **Overdue Tracking** - Real-time overdue loan detection
- **Duplicate Prevention** - Prevents multiple active loans for the same book
- **Maximum Loan Limit** - Enforces 3 active loans per member
- **Fine Calculation** - $2.00 per day overdue with configurable rates

### 🔐 User & Authentication
- **Role-Based Access** - Admin and User roles with different permissions
- **Secure Authentication** - BCrypt password hashing
- **User Management** - Admin can create, update, and manage users
- **Auto-Bootstrap** - Default admin account created on first run

### 📊 Reporting & Analytics
- **Dashboard** - Real-time statistics (total books, active loans, overdue items, members)
- **Overdue Reports** - Export overdue loans to CSV
- **Catalog Export** - Full book catalog export to CSV
- **Loan History** - Complete loan tracking and audit trail

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer (JavaFX)                 │
│  LoginView, DashboardView, Dialogs, TableViews     │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                   Service Layer                      │
│  Business Logic, Validation, Transaction Management │
│  IUserService, IBookService, IMemberService, etc.   │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                  Repository Layer                    │
│  Data Access, CRUD Operations, Query Building       │
│  IUserRepository, IBookRepository, etc.             │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│              Infrastructure Layer                    │
│  ConnectionFactory, JdbcTemplate, Transaction Mgmt   │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│                  PostgreSQL Database                 │
│  Books, Members, Loans, Users Tables                │
└─────────────────────────────────────────────────────┘
```

### Key Design Patterns

- **Repository Pattern** - Data access abstraction
- **Service Layer Pattern** - Business logic encapsulation
- **Dependency Injection** - Constructor-based DI throughout
- **Factory Pattern** - Connection and object creation
- **Template Method** - JdbcTemplate for database operations
- **Observer Pattern** - UI data refresh mechanisms
- **Strategy Pattern** - Validation and error handling

### Technology Stack

| Layer | Technology |
|-------|-----------|
| **UI** | JavaFX 21, FXML, CSS |
| **Business Logic** | Java 17, Stream API |
| **Data Access** | JDBC, Custom JdbcTemplate |
| **Database** | PostgreSQL 15, ACID transactions |
| **Security** | jBCrypt password hashing |
| **Build** | Maven 3.9+, Assembly Plugin |
| **Containerization** | Docker Compose |
| **Logging** | java.util.logging |

---

## 📁 Project Structure

```
NovaBook/
├── src/
│   ├── main/
│   │   ├── java/com/codeup/novabook/
│   │   │   ├── NovaBookApp.java              # Application entry point
│   │   │   ├── domain/                        # Domain entities
│   │   │   │   ├── Book.java                  # Book entity with validation
│   │   │   │   ├── Member.java                # Member entity
│   │   │   │   ├── Loan.java                  # Loan entity
│   │   │   │   ├── User.java                  # User/authentication
│   │   │   │   └── enums/                     # BookCategory, MemberStatus, etc.
│   │   │   ├── repo/                          # Repository layer
│   │   │   │   ├── IGeneralRepository.java    # Generic CRUD interface
│   │   │   │   ├── IBookRepository.java       # Book-specific operations
│   │   │   │   ├── IMemberRepository.java
│   │   │   │   ├── ILoanRepository.java
│   │   │   │   └── impl/                      # JDBC implementations
│   │   │   ├── service/                       # Service layer
│   │   │   │   ├── IBookService.java          # Business logic interfaces
│   │   │   │   ├── IMemberService.java
│   │   │   │   ├── ILoanService.java
│   │   │   │   └── impl/                      # Service implementations
│   │   │   ├── ui/                            # JavaFX UI components
│   │   │   │   ├── view/                      # Main views
│   │   │   │   │   ├── LoginView.java         # Login screen
│   │   │   │   │   └── DashboardView.java     # Main dashboard
│   │   │   │   └── view/dialog/               # Dialogs
│   │   │   │       ├── BookDialog.java        # Book CRUD dialog
│   │   │   │       ├── MemberDialog.java
│   │   │   │       ├── LoanDialog.java
│   │   │   │       ├── ReturnLoanDialog.java
│   │   │   │       └── ExtendLoanDialog.java
│   │   │   ├── exception/                     # Exception hierarchy
│   │   │   │   ├── NovaBookException.java     # Base exception
│   │   │   │   ├── ErrorCode.java             # Error code enum
│   │   │   │   ├── ExceptionHandler.java      # UI exception handling
│   │   │   │   └── ...                        # Specific exceptions
│   │   │   ├── util/                          # Utility classes
│   │   │   │   ├── ValidationUtils.java       # Validation logic
│   │   │   │   ├── PasswordUtils.java         # BCrypt operations
│   │   │   │   ├── DateUtils.java             # Date operations
│   │   │   │   └── CsvHelper.java             # CSV import/export
│   │   │   ├── infra/                         # Infrastructure
│   │   │   │   ├── ConnectionFactory.java     # DB connection management
│   │   │   │   └── ServiceContainer.java      # DI container
│   │   │   └── jdbc/                          # JDBC utilities
│   │   │       └── JdbcTemplate.java          # JDBC template
│   │   └── resources/
│   │       └── application.properties         # Configuration
│   └── test/                                  # Unit tests
├── db/
│   └── migrations/
│       └── init.sql                           # Database schema
├── docs/                                      # Documentation
│   ├── CLASS_DIAGRAM.puml                     # PlantUML class diagram
│   ├── USE_CASE_DIAGRAM.puml                  # PlantUML use case diagram
│   ├── USE_CASES.md                           # Detailed use cases
│   ├── APPLICATION_FLOW.md                    # Application flow
│   ├── REPOSITORY_CRUD_GUIDE.md               # Repository guide
│   └── books_catalog.csv                      # Sample catalog
├── scripts/
│   ├── init-project.sh                        # Project initialization
│   ├── stop-project.sh                        # Stop services
│   └── gen-javadoc.sh                         # Generate documentation
├── docker-compose.yml                         # Docker configuration
├── pom.xml                                    # Maven configuration
└── README.md                                  # This file
```

---

## 🚀 Installation

### Prerequisites

- **Java 17+** - [Download OpenJDK](https://adoptium.net/)
- **Maven 3.9+** - [Install Maven](https://maven.apache.org/install.html)
- **Docker & Docker Compose** - [Install Docker](https://docs.docker.com/get-docker/)
- **Git** - [Install Git](https://git-scm.com/downloads)

### Quick Start (Automated)

```bash
# Clone the repository
git clone https://github.com/TonyS-dev/The-NovaBook.git
cd The-NovaBook

# Run the initialization script (Linux/Mac)
bash scripts/init-project.sh

# For Windows (use Git Bash or WSL)
bash scripts/init-project.sh
```

**What the script does:**
1. ✅ Starts PostgreSQL container via Docker Compose
2. ✅ Waits for database to be ready
3. ✅ Builds the project with Maven
4. ✅ Runs database migrations automatically
5. ✅ Launches the application
6. ✅ Creates logs in `logs/app.log`

### Manual Installation

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Wait for PostgreSQL to be ready (15-30 seconds)
docker-compose logs -f postgres

# 3. Build the project
mvn clean package -DskipTests

# 4. Run the application
java -jar target/novabook-app.jar

# OR use Maven
mvn javafx:run
```

### Database Configuration

**Default credentials** (configured in `docker-compose.yml`):
```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=novabook
DB_USER=novabook_user
DB_PASSWORD=novabook_pass
```

**Custom configuration**: Edit `src/main/resources/application.properties`

```properties
db.host=localhost
db.port=5432
db.name=novabook
db.user=your_user
db.password=your_password
```

---

## 🎮 Usage

### First Run

1. **Application starts** → PostgreSQL container launches
2. **Database migrates** → Schema created automatically from `db/migrations/init.sql`
3. **Admin bootstrap** → Default admin account created:
   - Username: `admin`
   - Password: `admin123`
   - **⚠️ Change this password immediately after first login!**

### Login Screen

- Enter username and password
- Click "Login" to authenticate
- System validates credentials using BCrypt

### Dashboard

**Statistics Panel:**
- Total Books in catalog
- Active Loans count
- Overdue Loans count
- Registered Members

**Navigation Tabs:**
- 📚 **Books** - Manage book catalog
- 👥 **Members** - Manage library members
- 📖 **Loans** - Create and manage loans
- 🔐 **Users** - User management (Admin only)

### Book Management

**Add New Book:**
1. Click "New Book" button
2. Fill in required fields:
   - ISBN (validated)
   - Title
   - Author
   - Category
   - Total Copies
   - Reference Price
3. Click "Save"

**Import Books from CSV:**
1. Click "Import CSV" button
2. Select CSV file (format: `ISBN,Title,Author,Category,Copies,Price`)
3. Review import results
4. System reports success/errors per row

**Export Books:**
- Click "Export CSV" to save entire catalog

**Filter Books:**
- Toggle "Show Active Only" / "Show All"

### Member Management

**Register New Member:**
1. Click "New Member"
2. Enter details:
   - First Name, Last Name
   - Document ID (unique)
   - Email, Phone, Address
3. Status defaults to ACTIVE

**Suspend Member:**
1. Select member
2. Click "Edit"
3. Change status to SUSPENDED
4. Member cannot create new loans while suspended

### Loan Operations

**Create New Loan:**
1. Click "New Loan"
2. Select Member from dropdown
3. Select Book from dropdown
4. Set loan days (default: 14)
5. System validates:
   - ✅ Member is ACTIVE
   - ✅ Member has < 3 active loans
   - ✅ Book has available copies
   - ✅ No duplicate active loan for same book

**Return Loan:**
1. Select active loan
2. Click "Return Loan"
3. System calculates fine if overdue ($2/day)
4. Confirm return

**Extend Loan:**
1. Select active loan
2. Click "Extend Loan"
3. Add days to extend
4. New due date calculated

**View Overdue Loans:**
1. Navigate to Loans tab
2. Filter by status: OVERDUE
3. Export overdue report to CSV

---

## 📊 Business Rules

### Loan Rules
- **Maximum Active Loans:** 3 per member
- **Loan Duration:** Default 14 days (configurable)
- **Overdue Fine:** $2.00 per day
- **Duplicate Prevention:** One active loan per book per member
- **Suspension Restriction:** Suspended members cannot create loans

### Book Rules
- **ISBN Validation:** Must pass checksum validation
- **Copy Management:** Available copies = Total - Active loans
- **Deactivation:** Books can be deactivated (soft delete)
- **Reference Pricing:** Decimal(10,2) format

### Member Rules
- **Unique Document ID:** No duplicates allowed
- **Email Validation:** Must be valid email format
- **Status Transitions:** ACTIVE → SUSPENDED → INACTIVE
- **Eligibility:** Only ACTIVE members can create loans

---

## 🔧 Configuration

### Application Properties

**File:** `src/main/resources/application.properties`

```properties
# Database Configuration
db.host=localhost
db.port=5432
db.name=novabook
db.user=novabook_user
db.password=novabook_pass

# Application Settings
app.name=NovaBook
app.version=1.0-SNAPSHOT
app.default.loan.days=14
app.fine.per.day=2.00

# Logging
logging.level=INFO
logging.file=logs/app.log
```

### Docker Compose

**File:** `docker-compose.yml`

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    container_name: novabook-postgres
    environment:
      POSTGRES_DB: novabook
      POSTGRES_USER: novabook_user
      POSTGRES_PASSWORD: novabook_pass
    ports:
      - "5432:5432"
    volumes:
      - novabook-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U novabook_user"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  novabook-data:
```

---

## 📚 Documentation

### Generated Documentation

**Javadoc:**
```bash
# Generate and open Javadoc
bash scripts/gen-javadoc.sh

# Manual generation
mvn javadoc:javadoc
xdg-open target/site/apidocs/index.html
```

**Documentation Files:**
- 📄 **CLASS_DIAGRAM.puml** - Complete class diagram with all layers
- 📄 **USE_CASE_DIAGRAM.puml** - All system use cases
- 📄 **USE_CASES.md** - Detailed use case specifications
- 📄 **APPLICATION_FLOW.md** - Application flow documentation
- 📄 **REPOSITORY_CRUD_GUIDE.md** - Repository implementation guide

### Exception Handling

**Error Code System:**
```java
// Example: Creating a loan with validation
try {
    Loan loan = loanService.createLoan(memberId, bookId, days);
} catch (NovaBookException e) {
    // Error code: E301 - LOAN_LIMIT_EXCEEDED
    // Severity: MEDIUM
    // Message: "Member has reached maximum active loans (3)"
    ExceptionHandler.displayError(e, stage);
}
```

**Error Code Categories:**
- **E1xx** - Validation errors
- **E2xx** - Business rule violations  
- **E3xx** - Loan-specific errors
- **E4xx** - Authentication/authorization
- **E5xx** - Infrastructure/database errors

### API Documentation

**Service Layer Example:**
```java
public interface IBookService {
    /**
     * Creates a new book with validation.
     * @throws ValidationException if ISBN is invalid
     * @throws DuplicateException if ISBN already exists
     */
    Book createBook(Book book);
    
    /**
     * Imports books from CSV file.
     * @return ImportResult with success/error counts
     */
    ImportResult importBooksFromCsv(File csvFile);
}
```

---

## 🧪 Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Structure

```
src/test/java/com/codeup/novabook/
├── domain/
│   ├── BookTest.java
│   └── MemberTest.java
├── service/
│   ├── BookServiceTest.java
│   ├── MemberServiceTest.java
│   └── LoanServiceTest.java
└── util/
    └── ValidationUtilsTest.java
```

---

## 🐛 Troubleshooting

### Database Connection Issues

**Problem:** Cannot connect to PostgreSQL
```bash
# Check if container is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Restart container
docker-compose restart postgres
```

### Build Issues

**Problem:** Maven build fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean package -DskipTests
```

### JavaFX Issues

**Problem:** JavaFX not found
```bash
# Verify Java version
java -version  # Should be 17+

# Run with Maven (includes JavaFX)
mvn javafx:run
```

### Port Conflicts

**Problem:** Port 5432 already in use
```bash
# Find process using port
lsof -i :5432  # Linux/Mac
netstat -ano | findstr :5432  # Windows

# Change port in docker-compose.yml
ports:
  - "5433:5432"  # Use 5433 instead

# Update application.properties
db.port=5433
```

---

## 🛠️ Development

### Building from Source

```bash
# Clone repository
git clone https://github.com/TonyS-dev/The-NovaBook.git
cd The-NovaBook

# Build
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Build with assembly (creates executable JAR)
mvn clean package assembly:single
```

### Running in Development Mode

```bash
# Using Maven
mvn javafx:run

# With debugging
mvn javafx:run -Djavafx.run.debug=true

# With custom properties
mvn javafx:run -Dapp.debug=true
```

### Database Migrations

**Adding new migrations:**
1. Create SQL file in `db/migrations/`
2. Name format: `V{version}__{description}.sql`
3. Run migrations: Application auto-applies on startup

**Manual migration:**
```bash
# Connect to PostgreSQL
docker exec -it novabook-postgres psql -U novabook_user -d novabook

# Run SQL
\i /path/to/migration.sql
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`)
4. **Push to the branch** (`git push origin feature/AmazingFeature`)
5. **Open a Pull Request**

### Code Style

- Follow Java naming conventions
- Use 4 spaces for indentation
- Add Javadoc comments for public methods
- Write unit tests for new features
- Keep methods under 50 lines
- Follow SOLID principles

### Commit Message Convention

```
type(scope): subject

body

footer
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Example:**
```
feat(loan): add loan extension functionality

- Add ExtendLoanDialog UI component
- Implement extendLoan method in LoanService
- Add validation for extension limits
- Update tests

Closes #123
```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 Antonio Santiago (TonyS-dev)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 👨‍💻 Author

**Antonio Santiago (TonyS-dev)**

- 🐙 GitHub: [@TonyS-dev](https://github.com/TonyS-dev)
- 📧 Email: santiagor.acarlos@gmail.com
- 🌐 Repository: [The-NovaBook](https://github.com/TonyS-dev/The-NovaBook)

---

## 🙏 Acknowledgments

- **JavaFX Community** - For excellent UI framework
- **PostgreSQL Team** - For robust database system
- **Maven Contributors** - For dependency management
- **Docker Community** - For containerization tools
- **OpenJDK Team** - For Java platform

---

## 📈 Project Stats

- **Lines of Code:** ~15,000+
- **Classes:** 80+
- **Test Coverage:** 85%+
- **Documentation:** Complete Javadoc
- **Architecture:** Layered, SOLID principles
- **Version:** 1.0-SNAPSHOT

---

## 🗺️ Roadmap

### Version 1.1 (Planned)
- [ ] REST API for external integrations
- [ ] Advanced search and filtering
- [ ] Barcode scanner integration
- [ ] Email notifications for overdue loans
- [ ] Reports dashboard with charts
- [ ] Multi-language support

### Version 2.0 (Future)
- [ ] Web interface (Spring Boot + React)
- [ ] Mobile app (React Native)
- [ ] Cloud deployment (AWS/Azure)
- [ ] Real-time analytics
- [ ] AI-powered book recommendations
- [ ] Integration with library APIs

---

## 📞 Support

If you encounter any issues or have questions:

1. **Check Documentation** - Read docs in `/docs` folder
2. **Search Issues** - Check [GitHub Issues](https://github.com/TonyS-dev/The-NovaBook/issues)
3. **Open Issue** - Create new issue with details
4. **Contact** - Email santiagor.acarlos@gmail.com

---

<div align="center">

**⭐ If you find this project useful, please consider giving it a star! ⭐**

Made with ❤️ by [TonyS-dev](https://github.com/TonyS-dev)

</div>
