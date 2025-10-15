# Repository Layer - CRUD Operations Guide

## 📋 Overview

This document provides comprehensive guidance on CRUD operations, soft delete patterns, password hashing, and best practices for the NovaBook repository layer.

---

## 🗂️ CRUD Operations

### **1. CREATE (Create)**

Creates a new entity in the database.

#### **User Example:**
```java
// Service layer responsibility
User newUser = new User();
newUser.setName("John Doe");
newUser.setEmail("john@example.com");
newUser.setPassword(PasswordUtils.hashPassword("SecurePass123!")); // ← Hash first!
newUser.setPhone("1234567890");
newUser.setRole(UserRole.ASSISTANT);
newUser.setStatus(UserStatus.ACTIVE);

User savedUser = userRepository.create(newUser);
// savedUser now has ID and created_at populated by database
```

#### **Member Example:**
```java
Member newMember = new Member();
newMember.setFirstName("Jane");
newMember.setLastName("Smith");
newMember.setDocumentId("12345678");
newMember.setEmail("jane@example.com");
newMember.setPhone("9876543210");
newMember.setAddress("123 Main St");
newMember.setStatus(MemberStatus.ACTIVE);

Member savedMember = memberRepository.create(newMember);
// savedMember now has ID and registration_date populated
```

#### **Book Example:**
```java
Book newBook = new Book();
newBook.setIsbn("978-3-16-148410-0");
newBook.setTitle("One Hundred Years of Solitude");
newBook.setAuthor("Gabriel García Márquez");
newBook.setCategory("Literature");
newBook.setTotalCopies(5);
newBook.setAvailableCopies(5);
newBook.setReferencePrice(new BigDecimal("45000"));
newBook.setActive(true);

Book savedBook = bookRepository.create(newBook);
```

#### **Loan Example:**
```java
Loan newLoan = new Loan();
newLoan.setMemberId(memberId);
newLoan.setBookId(bookId);
newLoan.setLoanDate(LocalDate.now());
newLoan.setExpectedReturnDate(LocalDate.now().plusDays(14));
newLoan.setLoanDays(14);
newLoan.setStatus(LoanStatus.ACTIVE);

Loan savedLoan = loanRepository.create(newLoan);
```

---

### **2. READ (Read/Retrieve)**

#### **Find by ID:**
```java
Optional<User> userOpt = userRepository.findById(userId);
User user = userOpt.orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
```

#### **Find All:**
```java
List<User> allUsers = userRepository.findAll();
List<Member> allMembers = memberRepository.findAll();
List<Book> allBooks = bookRepository.findAll();
List<Loan> allLoans = loanRepository.findAll();
```

#### **Custom Queries:**
```java
// Find user by email (authentication)
Optional<User> user = userRepository.findByEmail("admin@library.com");

// Find active members
List<Member> activeMembers = memberRepository.findActiveMembers();

// Find available books
List<Book> availableBooks = bookRepository.findAvailableBooks();

// Find overdue loans (for CSV export)
List<Loan> overdueLoans = loanRepository.findOverdueLoans();

// Check if member has active loans
boolean hasLoans = loanRepository.hasActiveLoansByMember(memberId);
```

---

### **3. UPDATE (Full Update)**

Updates all fields of an entity.

#### **Full Update Example:**
```java
User user = userRepository.findById(userId).orElseThrow();
user.setName("Updated Name");
user.setEmail("newemail@example.com");
user.setPhone("5555555555");
user.setRole(UserRole.ADMIN);
user.setStatus(UserStatus.ACTIVE);

User updatedUser = userRepository.update(user);
// updated_at timestamp is automatically set by database trigger
```

⚠️ **Warning:** Full update overwrites ALL fields. Use partial update methods for single-field updates.

---

### **4. UPDATE (Partial Update)**

Updates only specific fields without overwriting others.

#### **Update User Password:**
```java
// Service layer hashes the new password first
String hashedPassword = PasswordUtils.hashPassword("NewSecurePassword456!");
boolean success = userRepository.updatePassword(userId, hashedPassword);
```

#### **Update User Email:**
```java
// Service layer validates email first
ValidationUtils.validateEmail("newemail@example.com");
boolean success = userRepository.updateEmail(userId, "newemail@example.com");
```

#### **Update User Phone:**
```java
boolean success = userRepository.updatePhone(userId, "9999999999");
```

#### **Update User Status:**
```java
boolean success = userRepository.updateStatus(userId, UserStatus.INACTIVE);
```

#### **Update Member Status:**
```java
// Suspend a member for overdue loans
boolean success = memberRepository.updateStatus(memberId, MemberStatus.SUSPENDED);
```

#### **Update Member Email:**
```java
boolean success = memberRepository.updateEmail(memberId, "newemail@example.com");
```

#### **Update Member Phone:**
```java
boolean success = memberRepository.updatePhone(memberId, "1112223333");
```

#### **Update Member Address:**
```java
boolean success = memberRepository.updateAddress(memberId, "456 New Street");
```

#### **Update Book Active Status:**
```java
// Deactivate a book (soft delete alternative for books)
boolean success = bookRepository.updateActiveStatus(bookId, false);
```

#### **Update Book Stock (Critical for Loans):**
```java
// During loan creation (should be in transaction with loan.create())
boolean success = bookRepository.decreaseAvailableCopies(bookId);

// During loan return (should be in transaction with loan.update())
boolean success = bookRepository.increaseAvailableCopies(bookId);
```

---

### **5. DELETE (Soft Delete)**

**IMPORTANT:** NovaBook uses **soft delete** to preserve data integrity and historical records.

#### **What is Soft Delete?**
- Records are NOT physically removed from database
- Instead, a status field is updated to mark the record as "deleted"
- Preserves foreign key relationships
- Allows data recovery and audit trails

#### **Soft Delete Patterns by Entity:**

##### **Users: Status = DELETED**
```java
userRepository.delete(userId);
// Executes: UPDATE users SET status = 'DELETED', updated_at = CURRENT_TIMESTAMP WHERE id = ?
```

##### **Members: Status = DELETED**
```java
memberRepository.delete(memberId);
// Executes: UPDATE members SET status = 'DELETED', updated_at = CURRENT_TIMESTAMP WHERE id = ?
```

##### **Books: is_active = FALSE**
```java
bookRepository.delete(bookId);
// Executes: UPDATE books SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?
```

##### **Loans: CANNOT BE DELETED!**
```java
loanRepository.delete(loanId);
// ❌ Throws UnsupportedOperationException
// Loans are historical records and should NEVER be deleted
// Use loan status (ACTIVE, RETURNED, OVERDUE) to manage lifecycle
```

#### **Why Soft Delete?**

1. **Foreign Key Integrity:**
   - Deleting a user would break existing loans references
   - Deleting a member would lose loan history
   - Deleting a book would lose circulation data

2. **Audit Trail:**
   - Who created the loan?
   - When was the member registered?
   - What books were in the catalog?

3. **Business Requirements:**
   - NovaBook requirements state: "maintain historical loan records"
   - Members might be reactivated later
   - Books might be reordered and reactivated

4. **Reporting:**
   - Member registration trends
   - Book popularity over time
   - Fine collection reports

---

## 🎯 Complete CRUD Example

### **User Registration Flow:**

```java
public class UserService {
    private final IUserRepository userRepository;
    
    public User registerUser(String name, String email, String plainPassword, String phone) {
        // 1. VALIDATION
        ValidationUtils.validateUser(name, email, plainPassword, phone);
        
        // 2. CHECK UNIQUENESS
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }
        
        // 3. HASH PASSWORD (Service responsibility!)
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        
        // 4. CREATE ENTITY
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setPhone(phone);
        user.setRole(UserRole.ASSISTANT);
        user.setStatus(UserStatus.ACTIVE);
        
        // 5. PERSIST (Repository responsibility!)
        return userRepository.create(user);
    }
    
    public User updateUserProfile(Integer userId, String newName, String newPhone) {
        // 1. CHECK EXISTS
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        // 2. VALIDATE
        ValidationUtils.validateName(newName);
        ValidationUtils.validatePhone(newPhone);
        
        // 3. PARTIAL UPDATE (only changed fields)
        userRepository.updatePhone(userId, newPhone);
        
        // 4. FULL UPDATE (if multiple fields changed)
        user.setName(newName);
        user.setPhone(newPhone);
        return userRepository.update(user);
    }
    
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        // 1. FIND USER
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // 2. VERIFY OLD PASSWORD
        if (!PasswordUtils.checkPassword(oldPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid current password");
        }
        
        // 3. VALIDATE NEW PASSWORD
        ValidationUtils.validatePassword(newPassword);
        
        // 4. HASH NEW PASSWORD
        String hashedPassword = PasswordUtils.hashPassword(newPassword);
        
        // 5. UPDATE
        userRepository.updatePassword(userId, hashedPassword);
    }
    
    public void deactivateUser(Integer userId) {
        // Soft delete via status update
        userRepository.updateStatus(userId, UserStatus.INACTIVE);
    }
    
    public void deleteUser(Integer userId) {
        // Soft delete
        userRepository.delete(userId);
        // User status is now DELETED but record still exists
    }
}
```

---

## ✅ Best Practices Checklist

### **CREATE Operations:**
- [ ] Validate all input using `ValidationUtils`
- [ ] Check uniqueness constraints (email, documentId, ISBN)
- [ ] Hash passwords in SERVICE layer before repository
- [ ] Set default status (ACTIVE for users/members, ACTIVE for loans)
- [ ] Let database auto-generate IDs and timestamps

### **READ Operations:**
- [ ] Use `Optional<T>` for single results (may not exist)
- [ ] Use `List<T>` for multiple results (may be empty)
- [ ] Handle `Optional.empty()` gracefully
- [ ] Consider filtering deleted records in queries

### **UPDATE Operations:**
- [ ] Use partial update methods for single-field changes
- [ ] Use full update for multiple-field changes
- [ ] Validate data before updating
- [ ] Check uniqueness if updating email/documentId/ISBN
- [ ] Re-hash password if updating password

### **DELETE Operations:**
- [ ] Use soft delete (status update) for Users and Members
- [ ] Use is_active=false for Books
- [ ] NEVER delete Loans (historical data)
- [ ] Consider cascade effects on foreign keys
- [ ] Log delete operations for audit trail

---

## 🚀 Next Steps

After verifying CRUD operations:
1. **Service Layer** - Business logic, transactions, validation
2. **Exception Layer** - Custom exceptions for business rules
3. **UI Layer** - JOptionPane interfaces
4. **CSV Export** - Overdue loans reporting
5. **Unit Tests** - JUnit tests for repositories

---

**Created by:** TonyS-dev/Antonio Santiago  
**Version:** 1.0  
**Date:** October 14, 2025  
**Project:** NovaBook - Library Management System
