# 📋 NovaBook - Application Flow

## 🎯 General Architecture

### Technology Stack
- **Frontend**: JavaFX (Similar to previous project)
- **Backend**: Java SE 17+ with layered architecture
- **Database**: PostgreSQL with JDBC
- **Dependency Management**: Maven
- **Testing**: JUnit 5

---

## 👥 ROLES AND PERMISSIONS

### 1. 🔴 ADMIN (Administrator)
**Description**: Has full access to the system, can manage users, members, books, and loans.

**Permissions**:
- ✅ **User Management**: Create, edit, list users. Assign roles.
- ✅ **Member Management**: Create, edit, activate, suspend, delete members.
- ✅ **Book Management**: Register, edit, activate/deactivate books. View low stock.
- ✅ **Loan Management**: Create, return, extend, view all loans.
- ✅ **Exports**: Export full catalog and overdue loans to CSV.
- ✅ **Reports**: View complete system statistics.
- ✅ **Configuration**: Access system parameters.

**Dashboard View**:
```
┌─────────────────────────────────────────────────────────┐
│  NovaBook - Dashboard                    [Admin] 👨‍💼    │
├─────────────────────────────────────────────────────────┤
│  Welcome: Juan Pérez (ADMINISTRATOR)                    │
├─────────────────────────────────────────────────────────┤
│  [Tab 1] 📚 Book Catalog                                │
│  [Tab 2] 👥 Member Management                           │
│  [Tab 3] 📖 Loan Management                             │
│  [Tab 4] 👨‍💼 User Management        ← ADMIN ONLY        │
│  [Tab 5] 📊 Reports and Exports                         │
│  [Tab 6] ⚙️  System Configuration   ← ADMIN ONLY        │
│                                                          │
│  [🔒 Log Out]                                           │
└─────────────────────────────────────────────────────────┘
```

---

### 2. 🟡 ASSISTANT (Assistant/Staff)
**Description**: Library staff who handle daily operations: loans, returns, and member registration.

**Permissions**:
- ✅ **Member Management**: Register new members, edit profiles, view lists.
- ✅ **Book Management**: View catalog, search books. **Cannot register/edit books**.
- ✅ **Loan Management**: Create loans, process returns, extend deadlines.
- ✅ **Queries**: Search books, members, active loans.
- ❌ **Cannot**: Manage users, deactivate books, export administrative reports.

**Dashboard View**:
```
┌─────────────────────────────────────────────────────────┐
│  NovaBook - Dashboard                 [Assistant] 👤    │
├─────────────────────────────────────────────────────────┤
│  Welcome: María González (ASSISTANT)                    │
├─────────────────────────────────────────────────────────┤
│  [Tab 1] 📚 Book Catalog (Read-only)                    │
│  [Tab 2] 👥 Member Management                           │
│  [Tab 3] 📖 Loan Management                             │
│                                                          │
│  [🔒 Log Out]                                           │
└─────────────────────────────────────────────────────────┘
```

---

### 3. 🟢 MEMBER (Library Member)
**Description**: End user who can browse the catalog and view their loan history.

**Permissions**:
- ✅ **Catalog**: View available books, search by title/author/category.
- ✅ **My Loans**: View active loans and history.
- ✅ **My Profile**: View and edit personal info (email, phone, address).
- ❌ **Cannot**: Create loans (must request at desk), manage other members, access reports.

**Dashboard View**:
```
┌─────────────────────────────────────────────────────────┐
│  NovaBook - My Library               [Member] 📖       │
├─────────────────────────────────────────────────────────┤
│  Welcome: Carlos Ramírez (MEMBER)                       │
├─────────────────────────────────────────────────────────┤
│  [Tab 1] 📚 Book Catalog (Read-only)                    │
│  [Tab 2] 📖 My Loans                                    │
│  [Tab 3] 👤 My Profile                                  │
│                                                          │
│  [🔒 Log Out]                                           │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 MAIN APPLICATION FLOW

### 1. Application Startup

```
┌─────────────────────────────────────────────────────────┐
│                   NovaBookApp.java                       │
│                     (MAIN CLASS)                         │
└─────────────────────────────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────┐
        │ 1. Load config.properties         │
        │    - db.url, db.user, db.password │
        │    - loanDays=7                   │
        │    - finePerDay=1500              │
        └───────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────┐
        │ 2. Initialize Database            │
        │    - Run migrations               │
        │    - Create default admin user    │
        └───────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────┐
        │ 3. Instantiate Services           │
        │    - UserService                  │
        │    - MemberService                │
        │    - BookService                  │
        │    - LoanService                  │
        └───────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────┐
        │ 4. Create ServiceContainer        │
        │    - Inject all services          │
        └───────────────────────────────────┘
                            ↓
        ┌───────────────────────────────────┐
        │ 5. Show LoginView                 │
        │    - Main JavaFX Stage            │
        └───────────────────────────────────┘
```

---

### 2. Login/Register View

```
┌─────────────────────────────────────────────────────────┐
│                   NovaBook - Login                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│              📚 Library System 📚                        │
│                                                          │
│  ┌────────────────────────────────────────────┐         │
│  │ Email:    [____________________________]   │         │
│  │ Password: [____________________________]   │         │
│  │                                            │         │
│  │           [   Log In   ]                   │         │
│  │                                            │         │
│  │  Don't have an account? [Register]         │         │
│  │                                            │         │
│  │           [   Test Mode 🧪   ]             │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
└─────────────────────────────────────────────────────────┘

Test Mode:
- Option 1: Admin (admin@mail.com / Admin123!)
- Option 2: Assistant (asistente@mail.com / Asist123!)
- Option 3: Member (socio@mail.com / Socio123!)
```

**Login Flow**:
```
User enters credentials
    ↓
UserService.authenticate(email, password)
    ↓
Authentication successful?
    ├─ YES → ServiceContainer.setCurrentUser(user)
    │         ↓
    │     Redirect to DashboardView by role
    │         ↓
    │     ┌─ ADMIN → Full dashboard (6 tabs)
    │     ├─ ASSISTANT → Limited dashboard (3 tabs)
    │     └─ MEMBER → Personal dashboard (3 tabs)
    │
    └─ NO → Show error "Invalid credentials"
            Return to LoginView
```

**Registration Flow**:
```
User clicks "Register"
    ↓
Show extended form:
  - Full name
  - Email
  - Password
  - Phone
    ↓
UserService.register(name, email, password, phone)
    ↓
Apply decorator:
  - role = ASSISTANT (default)
  - status = ACTIVE
  - createdAt = now()
    ↓
Registration successful → Message "You can now log in"
    ↓
Switch to Login mode
```

---

### 3. Dashboard View (Role-Based)

#### 3.1 📚 **Tab: Book Catalog**

**For ADMIN and ASSISTANT**:
```
┌─────────────────────────────────────────────────────────┐
│  📚 BOOK CATALOG                                         │
├─────────────────────────────────────────────────────────┤
│  [➕ New Book] [🔍 Search] [📂 Filter] [🔄 Refresh]      │ ← ADMIN only
├─────────────────────────────────────────────────────────┤
│  ┌───┬─────────────┬──────────────┬───────────┬────────┐│
│  │ID │ ISBN        │ Title        │ Author    │ Stock  ││
│  ├───┼─────────────┼──────────────┼───────────┼────────┤│
│  │1  │978-123-456  │Cien Años...  │García M.  │ 3/5 ✅ ││
│  │2  │978-789-012  │Don Quijote   │Cervantes  │ 0/2 ❌ ││
│  │3  │978-345-678  │El Principito │Saint-Ex.  │ 5/5 ✅ ││
│  └───┴─────────────┴──────────────┴───────────┴────────┘│
│                                                          │
│  [✏️  Edit] [🔴 Deactivate] [🟢 Activate] [📊 Stock]    │ ← ADMIN only
└─────────────────────────────────────────────────────────┘

ADMIN Operations:
- ➕ New Book: Dialog with full form
- ✏️  Edit: Dialog with pre-filled data
- 🔴 Deactivate: Confirmation + set isActive=false
- 🔍 Search: By ISBN, title, author
- 📂 Filter: By category
- 📊 Low Stock: Show books with < 3 copies
```

**For MEMBER (Read-only)**:
```
┌─────────────────────────────────────────────────────────┐
│  📚 BOOK CATALOG (Read-only)                             │
├─────────────────────────────────────────────────────────┤
│  [🔍 Search] [📂 Filter by Category]                     │
├─────────────────────────────────────────────────────────┤
│  ┌───┬─────────────┬──────────────┬───────────┬────────┐│
│  │ID │ ISBN        │ Title        │ Author    │ Avail. ││
│  ├───┼─────────────┼──────────────┼───────────┼────────┤│
│  │1  │978-123-456  │Cien Años...  │García M.  │  ✅    ││
│  │2  │978-789-012  │Don Quijote   │Cervantes  │  ❌    ││
│  └───┴─────────────┴──────────────┴───────────┴────────┘│
│                                                          │
│  [ℹ️  View Details]                                     │
└─────────────────────────────────────────────────────────┘
```

---

#### 3.2 👥 **Tab: Member Management**

**For ADMIN and ASSISTANT**:
```
┌─────────────────────────────────────────────────────────┐
│  👥 MEMBER MANAGEMENT                                   │
├─────────────────────────────────────────────────────────┤
│  [➕ New Member] [🔍 Search] [🔄 Refresh]                │
├─────────────────────────────────────────────────────────┤
│  ┌───┬────────────┬──────────────┬──────────┬─────────┐ │
│  │ID │ Document   │ Name         │ Email    │ Status  │ │
│  ├───┼────────────┼──────────────┼──────────┼─────────┤ │
│  │1  │12345678    │Juan Pérez    │juan@...  │ 🟢 ACT  │ │
│  │2  │87654321    │Ana López     │ana@...   │ 🔴 SUSP │ │
│  │3  │11223344    │Luis García   │luis@...  │ 🟢 ACT  │ │
│  └───┴────────────┴──────────────┴──────────┴─────────┘ │
│                                                          │
│  [✏️  Edit] [🔴 Suspend] [🟢 Activate] [✅ Eligible]    │
└─────────────────────────────────────────────────────────┘

Operations:
- ➕ New Member: Dialog with form (name, document, email, phone, address)
- ✏️  Edit: Dialog to update data
- 🔴 Suspend: Confirmation "Are you sure? Member cannot request loans"
- 🟢 Activate: Reactivate suspended member
- ✅ Eligible: Check if can request loans (active, no fines, < 3 loans)
```

**For MEMBER**:
```
No access to this tab
```

---

#### 3.3 📖 **Tab: Loan Management**

**For ADMIN and ASSISTANT**:
```
┌─────────────────────────────────────────────────────────┐
│  📖 LOAN MANAGEMENT                                     │
├─────────────────────────────────────────────────────────┤
│  [➕ New Loan] [🔄 Return] [⏰ Extend] [🔍]              │
├─────────────────────────────────────────────────────────┤
│  ┌───┬────────┬───────────┬────────────┬──────┬────────┐│
│  │ID │ Member │ Book      │ Due        │ Days │ Status ││
│  ├───┼────────┼───────────┼────────────┼──────┼────────┤│
│  │1  │Juan P. │Cien Años  │2024-10-20  │  3   │🟢 ACT  ││
│  │2  │Ana L.  │Quijote    │2024-10-10  │ -4   │🔴 OVER ││
│  │3  │Luis G. │Principito │2024-10-25  │  8   │🟢 ACT  ││
│  └───┴────────┴───────────┴────────────┴──────┴────────┘│
│                                                          │
│  [⚠️  View Overdue (2)] [💰 Calculate Fine] [📊 Stats]  │
└─────────────────────────────────────────────────────────┘

Operations:
- ➕ New Loan: 
    1. Search member by ID/email
    2. Check eligibility (active, no overdue loans)
    3. Search book by ISBN/title
    4. Check availability (stock > 0, isActive)
    5. Confirm loan (show summary)
    6. Execute TRANSACTION (decrease stock + create loan)
    
- 🔄 Return:
    1. Search loan by ID
    2. Calculate fine if overdue
    3. Confirm return (show fine if applicable)
    4. Execute TRANSACTION (update loan + increase stock)
    
- ⏰ Extend:
    1. Search active loan
    2. Request new due date
    3. Validate it's after current date
    4. Confirm extension
```

**For MEMBER**:
```
┌─────────────────────────────────────────────────────────┐
│  📖 MY LOANS                                            │
├─────────────────────────────────────────────────────────┤
│  [🟢 Active] [📋 History] [🔄 Refresh]                  │
├─────────────────────────────────────────────────────────┤
│  ACTIVE LOANS:                                          │
│  ┌───┬───────────────┬────────────┬──────┬────────┐     │
│  │ID │ Book          │ Due        │ Days │ Status │     │
│  ├───┼───────────────┼────────────┼──────┼────────┤     │
│  │1  │Cien Años...   │2024-10-20  │  3   │ 🟢 ACT │     │
│  │2  │El Principito  │2024-10-25  │  8   │ 🟢 ACT │     │
│  └───┴───────────────┴────────────┴──────┴────────┘     │
│                                                          │
│  ⚠️  Note: To return or extend, please go to the         │
│      library desk                                       │
└─────────────────────────────────────────────────────────┘
```

---

#### 3.4 👨‍💼 **Tab: User Management** (ADMIN ONLY)

```
┌─────────────────────────────────────────────────────────┐
│  👨‍💼 USER MANAGEMENT (Administrator)                    │
├─────────────────────────────────────────────────────────┤
│  [➕ New User] [🔍 Search] [🔄 Refresh]                  │
├─────────────────────────────────────────────────────────┤
│  ┌───┬──────────────┬──────────────┬────────┬─────────┐ │
│  │ID │ Name         │ Email        │ Role   │ Status  │ │
│  ├───┼──────────────┼──────────────┼────────┼─────────┤ │
│  │1  │Admin System  │admin@...     │ 🔴ADMIN│ 🟢 ACT  │ │
│  │2  │María G.      │maria@...     │ 🟡ASST │ 🟢 ACT  │ │
│  │3  │Pedro S.      │pedro@...     │ 🟡ASST │ 🔴 INAC │ │
│  └───┴──────────────┴──────────────┴────────┴─────────┘ │
│                                                          │
│  [✏️  Edit] [🔐 Reset Password] [🔴 Deactivate]         │
└─────────────────────────────────────────────────────────┘

Operations:
- ➕ New User: 
    Dialog with form:
    - Name, Email, Password, Phone
    - Role: [ADMIN | ASSISTANT]
    - Access Level (optional)
    Confirmation: "Create user with ADMIN role?"
    
- ✏️  Edit: Change name, email, phone
- 🔐 Reset Password: Assign new temporary password
- 🔴 Deactivate: Confirmation "Deactivate user? Will not be able to access system"
```

---

#### 3.5 📊 **Tab: Reports and Exports**

**For ADMIN**:
```
┌─────────────────────────────────────────────────────────┐
│  📊 REPORTS AND EXPORTS                                  │
├─────────────────────────────────────────────────────────┤
│  EXPORTS:                                               │
│  [📥 Export Full Catalog (CSV)]                         │
│  [📥 Export Overdue Loans (CSV)]                        │
│  [📥 Export Member List (CSV)]                          │
│                                                          │
│  STATISTICS:                                            │
│  ┌────────────────────────────────────────────┐         │
│  │ 📚 Total Books:            150             │         │
│  │ 📚 Available Books:        120             │         │
│  │ 📚 Loaned Books:            30             │         │
│  │ 👥 Active Members:         250             │         │
│  │ 📖 Active Loans:            30             │         │
│  │ ⚠️  Overdue Loans:            5             │         │
│  │ 💰 Pending Fines:       $7,500             │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  [📊 Generate Monthly Report] [🔍 Custom Query]          │
└─────────────────────────────────────────────────────────┘

Exports:
- Catalog → books_export_2024-10-17.csv
- Overdue Loans → overdue_loans_2024-10-17.csv
- Confirm save location
- Show success with full file path
```

**For ASSISTANT and MEMBER**:
```
No access to this tab
```

---

#### 3.6 ⚙️ **Tab: Configuration** (ADMIN ONLY)

```
┌─────────────────────────────────────────────────────────┐
│  ⚙️  SYSTEM CONFIGURATION                                │
├─────────────────────────────────────────────────────────┤
│  SYSTEM PARAMETERS:                                     │
│  ┌────────────────────────────────────────────┐         │
│  │ Loan Days:            [7  ] days           │         │
│  │ Fine per Day:         [$1500] COP          │         │
│  │ Max Loans/Member:     [3  ]                │         │
│  │ Low Stock Alert:      [3  ] copies         │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  DATABASE:                                              │
│  ┌────────────────────────────────────────────┐         │
│  │ URL: jdbc:postgresql://localhost:5432/...  │         │
│  │ User: novabook                             │         │
│  │ [🔌 Test Connection]                        │         │
│  └────────────────────────────────────────────┘         │
│                                                          │
│  [💾 Save Changes] [🔄 Restore Defaults]                 │
└─────────────────────────────────────────────────────────┘
```

---

## 🔔 CONFIRMATIONS AND VALIDATIONS

### Destructive Operations Requiring Confirmation:

1. **Deactivate Book**:
   ```
   ⚠️  Are you sure you want to deactivate this book?
   
   Title: "Cien Años de Soledad"
   ISBN: 978-123-456-789
   
   ⚠️  The book will no longer be available for new loans.
   Active loans will not be affected.
   
   [Yes, Deactivate]  [Cancel]
   ```

2. **Suspend Member**:
   ```
   ⚠️  Are you sure you want to suspend this member?
   
   Name: Juan Pérez
   Active Loans: 2
   
   ⚠️  The member will not be able to request new loans until
   their account is reactivated.
   
   [Yes, Suspend]  [Cancel]
   ```

3. **Delete User** (Soft Delete):
   ```
   ⚠️  Are you sure you want to delete this user?
   
   Name: María González
   Role: ASSISTANT
   
   ⚠️  The user will not be able to access the system.
   This operation cannot be undone.
   
   [Yes, Delete]  [Cancel]
   ```

4. **Create Loan**:
   ```
   ✅ CONFIRM LOAN
   
   Member: Juan Pérez (ID: 1)
   Book: "Cien Años de Soledad"
   Loan Date: 2024-10-17
   Due Date: 2024-10-24 (7 days)
   Fine per late day: $1,500
   
   [Confirm Loan]  [Cancel]
   ```

5. **Return with Fine**:
   ```
   ⚠️  RETURN WITH FINE
   
   Loan ID: 5
   Book: "Don Quijote de la Mancha"
   Due Date: 2024-10-10
   Return Date: 2024-10-17
   
   Days late: 7 days
   Calculated fine: $10,500
   
   ⚠️  The member must pay the fine before requesting
   new loans.
   
   [Process Return]  [Cancel]
   ```

---

## 📁 UI FILE STRUCTURE

```
src/main/java/com/codeup/novabook/ui/
├── view/
│   ├── BaseView.java              # Abstract base (Template Method)
│   ├── LoginView.java             # Login/Register + Test Mode
│   ├── DashboardView.java         # Role-based dashboard (TabPane)
│   └── dialog/
│       ├── BookDialog.java        # Register/Edit book
│       ├── MemberDialog.java      # Register/Edit member
│       ├── LoanDialog.java        # Create loan
│       ├── ReturnLoanDialog.java  # Return book
│       ├── ExtendLoanDialog.java  # Extend deadline
│       ├── UserDialog.java        # Manage user (ADMIN)
│       └── ConfirmDialog.java     # Generic confirmations
├── helper/
│   ├── TableFormatter.java        # TableView formatting
│   ├── AlertHelper.java           # Alert wrappers
│   ├── ValidationHelper.java      # UI validations
│   └── ExportHelper.java          # Export CSV
└── ServiceContainer.java          # DI Container
```

---
