# NovaBook - Use Case Specifications

**Version:** 1.0  
**Date:** October 14, 2025  
**Status:** Complete  

---

## Table of Contents

1. [Authentication Use Cases](#authentication-use-cases)
2. [Book Management Use Cases](#book-management-use-cases)
3. [Member Management Use Cases](#member-management-use-cases)
4. [Loan Management Use Cases](#loan-management-use-cases)
5. [Reporting Use Cases](#reporting-use-cases)
6. [System Use Cases](#system-use-cases)

---

## Authentication Use Cases

### UC-001: Login to System

**Actor:** Librarian/Admin  
**Preconditions:** User account exists and is ACTIVE  
**Postconditions:** User is authenticated and dashboard is displayed

**Main Flow:**
1. User enters email and password
2. System validates credentials
3. System checks user status (must be ACTIVE)
4. System updates last login timestamp
5. System displays dashboard

**Alternative Flows:**
- **A1:** Invalid credentials → Show error "Invalid email or password"
- **A2:** User is INACTIVE/SUSPENDED → Show error "Account is not active"

**Business Rules:**
- Password must be hashed using BCrypt
- Maximum 3 login attempts before temporary lock

---

### UC-002: Manage Users

**Actor:** Admin  
**Preconditions:** User is logged in with ADMIN role  
**Postconditions:** User account is created/updated/deactivated

**Main Flow:**
1. Admin selects "Users" tab
2. System displays list of all users
3. Admin can create/update/deactivate users

**Business Rules:**
- Only ADMIN role can manage users
- Cannot deactivate own account
- Email must be unique

---

## Book Management Use Cases

### UC-010: Register New Book

**Actor:** Librarian  
**Preconditions:** User is logged in  
**Postconditions:** Book is registered in system

**Main Flow:**
1. Librarian clicks "Add Book" button
2. System displays Book Dialog
3. Librarian enters: ISBN, Title, Author, Category, Stock, Reference Price
4. System validates ISBN format (10 or 13 digits)
5. System checks ISBN uniqueness
6. System validates Reference Price >= 0 (optional)
7. System creates book with status ACTIVE
8. System sets availableCopies = totalCopies

**Alternative Flows:**
- **A1:** ISBN already exists → Show error "ISBN already registered"
- **A2:** Invalid ISBN format → Show error "Invalid ISBN format"
- **A3:** Negative price → Show error "Price cannot be negative"
- **A4:** Empty required fields → Show error "All fields are required"

**Business Rules:**
- ISBN must be unique
- Reference Price is optional (can be null)
- Initial status is always ACTIVE
- availableCopies = totalCopies at creation

---

### UC-011: Update Book Information

**Actor:** Librarian  
**Preconditions:** Book exists in system  
**Postconditions:** Book information is updated

**Main Flow:**
1. Librarian selects book from table
2. Librarian clicks "Edit" button
3. System displays Book Dialog with current data
4. Librarian modifies fields
5. System validates changes
6. System updates book information

**Business Rules:**
- Cannot change ISBN to one that already exists
- Stock changes adjust totalCopies (availableCopies calculated)
- Reference Price can be added/updated/removed

---

### UC-012: Import Books from CSV

**Actor:** Librarian  
**Preconditions:** CSV file exists with correct format  
**Postconditions:** Books are imported to system

**Main Flow:**
1. Librarian clicks "Import CSV" button
2. System displays file chooser
3. Librarian selects CSV file
4. System reads CSV line by line
5. System validates each line
6. System creates books for valid lines
7. System displays import summary (success/errors)

**CSV Format:**
```
isbn,title,author,category,stock,reference_price
978-0132350884,Clean Code,Robert C. Martin,Programming,10,45000.00
978-0134685991,Effective Java,Joshua Bloch,Programming,5,
```

**Business Rules:**
- Field 6 (reference_price) is optional
- Invalid price format = error reported, line skipped
- Duplicate ISBN = error reported, line skipped
- All validations applied per line

---

### UC-013: Filter Active/Inactive Books

**Actor:** Librarian  
**Preconditions:** Books exist in system  
**Postconditions:** Table displays filtered books

**Main Flow:**
1. System loads Books tab
2. System displays only ACTIVE books by default
3. Librarian can toggle "Show Inactive Books" checkbox
4. System refreshes table with filter applied

**Business Rules:**
- Default: Show only ACTIVE books
- Toggle ON: Show ALL books (active + inactive)
- Filter applies to search results too

---

### UC-014: Deactivate Book

**Actor:** Librarian  
**Preconditions:** Book exists and is ACTIVE  
**Postconditions:** Book status is INACTIVE

**Main Flow:**
1. Librarian selects book from table
2. Librarian clicks "Delete" button
3. System checks if book is already INACTIVE
4. System displays confirmation dialog
5. Librarian confirms
6. System sets book status to INACTIVE

**Alternative Flows:**
- **A1:** Book already INACTIVE → Show warning "Book is already deactivated"
- **A2:** User cancels → No changes

**Business Rules:**
- Cannot deactivate book that is already INACTIVE
- Deactivation does not delete from database
- Inactive books can be activated again

---

## Member Management Use Cases

### UC-020: Register New Member

**Actor:** Librarian  
**Preconditions:** User is logged in  
**Postconditions:** Member is registered in system

**Main Flow:**
1. Librarian clicks "Add Member" button
2. System displays Member Dialog
3. Librarian enters: Name, Email, Phone, Address
4. System validates email format
5. System checks email uniqueness
6. System validates phone format (10 digits)
7. System creates member with status ACTIVE

**Business Rules:**
- Email must be unique
- Email format: valid email pattern
- Phone: exactly 10 digits
- Initial status is always ACTIVE
- Registration date = current date

---

### UC-021: Suspend Member

**Actor:** Librarian  
**Preconditions:** Member exists and is ACTIVE  
**Postconditions:** Member status is SUSPENDED

**Main Flow:**
1. Librarian selects member from table
2. Librarian clicks "Suspend" button
3. System displays confirmation dialog
4. Librarian confirms
5. System sets member status to SUSPENDED

**Business Rules:**
- Suspended members cannot create new loans
- Existing active loans remain active
- Can be reactivated later

---

### UC-022: Check Member Eligibility

**Actor:** System (called by Create Loan)  
**Preconditions:** Member exists  
**Postconditions:** Eligibility status returned

**Main Flow:**
1. System checks member status (must be ACTIVE)
2. System counts active loans for member
3. System checks if count < 3
4. System checks for overdue loans
5. System returns eligibility result

**Business Rules:**
- Member must be ACTIVE
- Member must have < 3 active loans
- Member must not have overdue loans

---

## Loan Management Use Cases

### UC-030: Create New Loan

**Actor:** Librarian  
**Preconditions:** Member and Book exist  
**Postconditions:** Loan is created, book stock decreased

**Main Flow:**
1. Librarian clicks "Create Loan" button
2. System displays Loan Dialog
3. System loads active members in ComboBox
4. System loads available books in ComboBox
5. Librarian selects member
6. Librarian selects book
7. Librarian sets due date
8. System validates member is ACTIVE
9. System validates member has < 3 active loans (**Round 2**)
10. System checks member doesn't have active loan of this book (**Round 2**)
11. System validates book is ACTIVE
12. System validates book has available copies
13. System starts database transaction
14. System decreases book available copies
15. System creates loan record (status = ACTIVE)
16. System commits transaction

**Alternative Flows:**
- **A1:** Member is INACTIVE → Show error "Member is not active"
- **A2:** Member has 3 active loans → Show error "Member has reached maximum active loans" (E153)
- **A3:** Member already has this book → Show error "Member already has an active loan of this book" (E154)
- **A4:** Book is INACTIVE → Show error "Book is not available"
- **A5:** Book has no stock → Show error "Book has no available copies"
- **A6:** Transaction fails → Rollback all changes

**Business Rules:**
- Max 3 active loans per member (hard-coded)
- No duplicate book loans for same member
- Loan date = current date
- Due date must be after loan date
- Available copies decremented atomically
- All operations in database transaction

---

### UC-031: Return Loan

**Actor:** Librarian  
**Preconditions:** Loan exists with status ACTIVE  
**Postconditions:** Loan is returned, book stock increased, fine calculated

**Main Flow:**
1. Librarian selects loan from table
2. Librarian clicks "Return" button
3. System displays Return Dialog with fine rate
4. Librarian enters fine rate per day
5. System starts database transaction
6. System checks loan status (must be ACTIVE)
7. System sets actual return date = current date
8. System calculates fine if overdue
9. System sets loan status = RETURNED
10. System increases book available copies
11. System updates loan record
12. System commits transaction

**Fine Calculation:**
- If return date > due date: fine = days * ratePerDay
- Otherwise: fine = 0

**Business Rules:**
- Can only return ACTIVE loans
- Fine calculated automatically if overdue
- Available copies incremented atomically
- All operations in database transaction

---

### UC-032: View Overdue Loans

**Actor:** Librarian  
**Preconditions:** User is logged in  
**Postconditions:** Overdue loans are displayed

**Main Flow:**
1. Librarian navigates to Loans tab
2. System loads all active loans
3. System filters loans where expectedReturnDate < current date
4. System displays overdue loans in table

**Business Rules:**
- Only ACTIVE loans can be overdue
- Overdue = expectedReturnDate < current date

---

## Reporting Use Cases

### UC-040: View Dashboard Statistics

**Actor:** Librarian  
**Preconditions:** User is logged in  
**Postconditions:** Statistics are displayed

**Main Flow:**
1. System displays dashboard tabs
2. Each tab shows relevant statistics
3. User can view counts and summaries

**Statistics:**
- Total books (active/inactive)
- Total members (active/suspended)
- Active loans count
- Overdue loans count

---

## System Use Cases

### UC-050: Auto-refresh Data on Load

**Actor:** System  
**Preconditions:** User opens dashboard  
**Postconditions:** All tabs display current data

**Main Flow:**
1. System creates Books tab
2. System calls loadBooks() automatically
3. System creates Members tab
4. System calls loadMembers() automatically
5. System creates Loans tab
6. System calls loadLoans() automatically
7. System creates Users tab (Admin only)
8. System calls loadUsers() automatically

**Business Rules:**
- All tabs auto-load data on creation
- No manual refresh required initially
- Refresh button available for updates

---

### UC-051: Validate Business Rules

**Actor:** System  
**Preconditions:** User attempts operation  
**Postconditions:** Validation result returned

**Main Flow:**
1. System receives operation request
2. System validates all business rules
3. System throws appropriate exception if invalid
4. System displays error message to user

**Validations:**
- Max 3 loans per member
- No duplicate book loans
- Email uniqueness
- ISBN uniqueness
- Phone format (10 digits)
- Email format
- Price >= 0
- Date ranges

---

### UC-052: Manage Database Transactions

**Actor:** System  
**Preconditions:** Multi-step operation required  
**Postconditions:** All operations succeed or all rollback

**Main Flow:**
1. System starts transaction
2. System executes all operations
3. If any fails: System rolls back all changes
4. If all succeed: System commits transaction

**Transactional Operations:**
- Create Loan (decrease stock + create record)
- Return Loan (increase stock + update record)
- All critical data modifications

---

### UC-053: Log System Events

**Actor:** System  
**Preconditions:** Event occurs  
**Postconditions:** Event is logged

**Main Flow:**
1. System detects event
2. System logs event with level (INFO/WARNING/SEVERE)
3. System includes timestamp and context
4. System writes to log file

**Logged Events:**
- User login/logout
- Book creation/update/deactivation
- Member registration/suspension
- Loan creation/return
- Errors and exceptions
- Transaction start/commit/rollback

---

## Error Codes Reference

| Code | Name | Message | Use Case |
|------|------|---------|----------|
| E100 | BOOK_NOT_FOUND | Book not found | UC-011, UC-013, UC-030 |
| E101 | ISBN_ALREADY_EXISTS | ISBN already registered | UC-010 |
| E110 | MEMBER_NOT_FOUND | Member not found | UC-021, UC-030 |
| E111 | EMAIL_ALREADY_EXISTS | Email already registered | UC-020 |
| E112 | MEMBER_INACTIVE | Member is not active | UC-030 |
| E150 | LOAN_NOT_FOUND | Loan not found | UC-031 |
| E151 | BOOK_NOT_AVAILABLE | Book is not available for loan | UC-030 |
| E153 | MAX_ACTIVE_LOANS_REACHED | Member has reached maximum active loans | UC-030 |
| E154 | DUPLICATE_BOOK_LOAN | Member already has an active loan of this book | UC-030 |
| E500 | INVALID_DATA | Invalid or incomplete data provided | All |
| E501 | INVALID_EMAIL_FORMAT | Invalid email format | UC-020 |
| E502 | INVALID_PHONE_FORMAT | Invalid phone number format | UC-020 |
| E505 | INVALID_ISBN_FORMAT | Invalid ISBN format | UC-010, UC-012 |

---

**Document Version:** 1.0  
**Last Updated:** October 14, 2025  
**Total Use Cases:** 20+
