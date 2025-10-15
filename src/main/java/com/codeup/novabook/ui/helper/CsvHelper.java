package com.codeup.novabook.ui.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.LoanStatus;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.LoanNotFoundException;
import com.codeup.novabook.exception.MemberAlreadyExistsException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.service.IBookService;
import com.codeup.novabook.service.ILoanService;
import com.codeup.novabook.service.IMemberService;

import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Utility class for CSV import/export operations (ADMIN only).
 * Handles book catalog import/export and overdue loans export.
 */
public class CsvHelper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private CsvHelper() {
        // Utility class, no instantiation
    }
    
    /**
     * Exports books catalog to CSV file
     */
    public static File exportBooksCatalog(List<Book> books, Window owner) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Books Catalog");
        fileChooser.setInitialFileName("books_catalog.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return null; // User cancelled
        }
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            
            // Write header (ID first for data integrity)
            writer.write("ID,ISBN,Title,Author,Category,Total Copies,Available Copies,Reference Price,Active");
            writer.newLine();
            
            // Write data
            for (Book book : books) {
                writer.write(String.valueOf(book.getId()));
                writer.write(",");
                writer.write(escapeCsv(book.getIsbn()));
                writer.write(",");
                writer.write(escapeCsv(book.getTitle()));
                writer.write(",");
                writer.write(escapeCsv(book.getAuthor()));
                writer.write(",");
                // Convert BookCategory enum to display name for CSV export
                writer.write(escapeCsv(book.getCategory() != null ? book.getCategory().getDisplayName() : ""));
                writer.write(",");
                writer.write(String.valueOf(book.getTotalCopies()));
                writer.write(",");
                writer.write(String.valueOf(book.getAvailableCopies()));
                writer.write(",");
                writer.write(book.getReferencePrice() != null ? book.getReferencePrice().toString() : "0.00");
                writer.write(",");
                writer.write(book.getActive() ? "Yes" : "No");
                writer.newLine();
            }
        }
        
        return file;
    }
    
    /**
     * Exports overdue loans to CSV file
     */
    public static File exportOverdueLoans(List<Loan> loans, 
                                         IMemberService memberService,
                                         IBookService bookService,
                                         Window owner) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Overdue Loans");
        fileChooser.setInitialFileName("overdue_loans.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return null; // User cancelled
        }
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            
            // Write header (include foreign key IDs for referential integrity)
            writer.write("Loan ID,Member ID,Member Name,Member Document,Book ID,Book Title,Book ISBN,Due Date,Days Overdue,Status");
            writer.newLine();
            
            // Write data
            for (Loan loan : loans) {
                try {
                    Member member = memberService.getMemberById(loan.getMemberId());
                    Book book = bookService.getBookById(loan.getBookId());
                    
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                        loan.getExpectedReturnDate(), 
                        java.time.LocalDate.now()
                    );
                    
                    writer.write(String.valueOf(loan.getId()));
                    writer.write(",");
                    writer.write(String.valueOf(loan.getMemberId()));
                    writer.write(",");
                    writer.write(escapeCsv(member.getFullName()));
                    writer.write(",");
                    writer.write(escapeCsv(member.getDocumentId()));
                    writer.write(",");
                    writer.write(String.valueOf(loan.getBookId()));
                    writer.write(",");
                    writer.write(escapeCsv(book.getTitle()));
                    writer.write(",");
                    writer.write(escapeCsv(book.getIsbn()));
                    writer.write(",");
                    writer.write(loan.getExpectedReturnDate().format(DATE_FORMATTER));
                    writer.write(",");
                    writer.write(String.valueOf(daysOverdue));
                    writer.write(",");
                    writer.write(loan.getStatus().name());
                    writer.newLine();
                } catch (BookNotFoundException | MemberNotFoundException | IOException e) {
                    // Skip loans with missing data
                    System.err.println("Error exporting loan " + loan.getId() + ": " + e.getMessage());
                }
            }
        }
        
        return file;
    }
    
    /**
     * Imports books from CSV file
     * @return ImportResult with success/error counts and messages
     */
    public static ImportResult importBooks(IBookService bookService, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Books from CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return null; // User cancelled
        }
        
        ImportResult result = new ImportResult();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            // Skip header
            String header = reader.readLine();
            if (header == null) {
                result.addError("Empty file");
                return result;
            }
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    String[] fields = parseCsvLine(line);
                    
                    if (fields.length < 5) {
                        result.addError("Line " + lineNumber + ": Insufficient fields (need ID, ISBN, Title, Author, Category minimum)");
                        continue;
                    }
                    
                    // Field 0: ID (optional - if exists, check for duplicates)
                    Integer bookId = null;
                    String idField = fields[0].trim();
                    if (!idField.isEmpty()) {
                        try {
                            bookId = Integer.valueOf(idField);
                        } catch (NumberFormatException e) {
                            result.addError("Line " + lineNumber + ": Invalid ID format: " + fields[0]);
                            continue;
                        }
                    }
                    
                    String isbn = fields[1].trim();
                    String title = fields[2].trim();
                    String author = fields[3].trim();
                    String category = fields.length > 4 ? fields[4].trim() : null;
                    int totalCopies = fields.length > 5 && !fields[5].trim().isEmpty() ? 
                               Integer.parseInt(fields[5].trim()) : 1;
                    // Note: Available copies (field 6) is managed by the loan system, not imported
                    
                    // Parse reference price (field 7)
                    java.math.BigDecimal referencePrice = null;
                    if (fields.length > 7 && !fields[7].trim().isEmpty()) {
                        try {
                            referencePrice = new java.math.BigDecimal(fields[7].trim());
                        } catch (NumberFormatException e) {
                            result.addError("Line " + lineNumber + ": Invalid price format: " + fields[7]);
                            continue;
                        }
                    }
                    
                    // Parse active status (field 8) - default to true if not specified
                    boolean active = true;
                    if (fields.length > 8 && !fields[8].trim().isEmpty()) {
                        String activeStr = fields[8].trim().toLowerCase();
                        active = activeStr.equals("yes") || activeStr.equals("true") || activeStr.equals("1");
                    }
                    
                    // Validate required fields
                    if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                        result.addError("Line " + lineNumber + ": ISBN, Title, and Author are required");
                        continue;
                    }
                    
                    // If ID exists, check if book with that ID already exists (skip duplicate)
                    if (bookId != null) {
                        try {
                            Book existingBook = bookService.getBookById(bookId);
                            if (existingBook != null) {
                                // Book already exists, skip
                                result.incrementSkipped();
                                continue;
                            }
                        } catch (BookNotFoundException e) {
                            // Book doesn't exist, continue with creation
                        }
                    }
                    
                    // Check if book already exists by ISBN
                    if (bookService.isbnExists(isbn)) {
                        result.addError("Line " + lineNumber + ": Book with ISBN " + isbn + " already exists");
                        result.incrementSkipped();
                        continue;
                    }
                    
                    // Register book with reference price
                    Book book = bookService.registerBook(title, author, isbn, category, totalCopies, referencePrice);
                    
                    // If book should be inactive, deactivate it
                    if (!active) {
                        bookService.deactivateBook(book.getId());
                    }
                    
                    result.incrementSuccess();
                    
                } catch (NumberFormatException e) {
                    result.addError("Line " + lineNumber + ": Invalid number format");
                } catch (BookNotFoundException | ValidationException e) {
                    result.addError("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            result.addError("Error reading file: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Escapes special characters for CSV
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Parses a CSV line handling quoted fields
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        
        return fields.toArray(String[]::new);
    }
    
    /**
     * Export all loans to CSV file
     */
    public static File exportLoans(List<Loan> loans, IMemberService memberService, 
                                   IBookService bookService, Window owner) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Loans");
        fileChooser.setInitialFileName("loans_export_" + 
                LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return null;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header (include foreign key IDs for referential integrity)
            writer.write("Loan ID,Member ID,Member Name,Member Email,Book ID,Book Title,ISBN,Loan Date,Due Date,Return Date,Status,Days Overdue");
            writer.newLine();
            
            // Write data
            for (Loan loan : loans) {
                try {
                    Member member = memberService.getMemberById(loan.getMemberId());
                    Book book = bookService.getBookById(loan.getBookId());
                    
                    writer.write(loan.getId().toString());
                    writer.write(",");
                    writer.write(loan.getMemberId().toString());
                    writer.write(",");
                    writer.write(escapeCsv(member.getFullName()));
                    writer.write(",");
                    writer.write(escapeCsv(member.getEmail()));
                    writer.write(",");
                    writer.write(loan.getBookId().toString());
                    writer.write(",");
                    writer.write(escapeCsv(book.getTitle()));
                    writer.write(",");
                    writer.write(escapeCsv(book.getIsbn()));
                    writer.write(",");
                    writer.write(loan.getLoanDate().format(DateTimeFormatter.ISO_DATE));
                    writer.write(",");
                    writer.write(loan.getExpectedReturnDate().format(DateTimeFormatter.ISO_DATE));
                    writer.write(",");
                    writer.write(loan.getActualReturnDate() != null ? 
                            loan.getActualReturnDate().format(DateTimeFormatter.ISO_DATE) : "");
                    writer.write(",");
                    writer.write(loan.getStatus().name());
                    writer.write(",");
                    // Calculate days overdue based on current date or return date
                    long daysOverdue = 0;
                    LocalDate comparisonDate = loan.getActualReturnDate() != null ? 
                            loan.getActualReturnDate() : LocalDate.now();
                    
                    if (comparisonDate.isAfter(loan.getExpectedReturnDate())) {
                        daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                                loan.getExpectedReturnDate(), comparisonDate);
                    }
                    writer.write(String.valueOf(daysOverdue));
                    writer.newLine();
                } catch (BookNotFoundException | MemberNotFoundException | IOException e) {
                    // Skip loans with missing member/book data
                }
            }
        }
        
        return file;
    }
    
    /**
     * Export members to CSV file
     */
    public static File exportMembers(List<Member> members, Window owner) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Members");
        fileChooser.setInitialFileName("members_export_" + 
                LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return null;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header (ID first for data integrity)
            writer.write("ID,Document Number,First Name,Last Name,Email,Phone,Address,Registration Date,Status");
            writer.newLine();
            
            // Write data
            for (Member member : members) {
                writer.write(String.valueOf(member.getId()));
                writer.write(",");
                writer.write(escapeCsv(member.getDocumentNumber()));
                writer.write(",");
                writer.write(escapeCsv(member.getFirstName()));
                writer.write(",");
                writer.write(escapeCsv(member.getLastName()));
                writer.write(",");
                writer.write(escapeCsv(member.getEmail()));
                writer.write(",");
                writer.write(escapeCsv(member.getPhone() != null ? member.getPhone() : ""));
                writer.write(",");
                writer.write(escapeCsv(member.getAddress() != null ? member.getAddress() : ""));
                writer.write(",");
                writer.write(member.getRegistrationDate().format(DateTimeFormatter.ISO_DATE));
                writer.write(",");
                writer.write(member.getStatus().name());
                writer.newLine();
            }
        }
        
        return file;
    }
    
    /**
     * Import loans from CSV file
     */
    public static int importLoans(ILoanService loanService, IMemberService memberService,
                                  IBookService bookService, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Loans");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return 0;
        }
        
        int importedCount = 0;
        int skippedCount = 0;
        StringBuilder errors = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Skip header
            int lineNumber = 1;
            String line;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 12) {
                        errors.append("Line ").append(lineNumber).append(": Not enough fields\n");
                        continue;
                    }
                    
                    // Expected format: Loan ID,Member ID,Member Name,Member Email,Book ID,Book Title,ISBN,Loan Date,Due Date,Return Date,Status,Days Overdue
                    // Field 0: Loan ID (check for duplicates)
                    Integer loanId = null;
                    String loanIdField = fields[0].trim();
                    if (!loanIdField.isEmpty()) {
                        try {
                            loanId = Integer.valueOf(loanIdField);
                        } catch (NumberFormatException e) {
                            errors.append("Line ").append(lineNumber).append(": Invalid Loan ID format: ").append(fields[0]).append("\n");
                            continue;
                        }
                    }
                    
                    // Field 1: Member ID (use this for referential integrity)
                    Integer memberId = null;
                    String memberIdField = fields[1].trim();
                    if (!memberIdField.isEmpty()) {
                        try {
                            memberId = Integer.valueOf(memberIdField);
                        } catch (NumberFormatException e) {
                            errors.append("Line ").append(lineNumber).append(": Invalid Member ID format: ").append(fields[1]).append("\n");
                            continue;
                        }
                    }
                    
                    // Field 4: Book ID (use this for referential integrity)
                    Integer bookId = null;
                    String bookIdField = fields[4].trim();
                    if (!bookIdField.isEmpty()) {
                        try {
                            bookId = Integer.valueOf(bookIdField);
                        } catch (NumberFormatException e) {
                            errors.append("Line ").append(lineNumber).append(": Invalid Book ID format: ").append(fields[4]).append("\n");
                            continue;
                        }
                    }
                    
                    // Field 7: Loan Date
                    // Field 8: Due Date
                    // Field 9: Return Date
                    // Field 10: Status
                    String loanDateStr = fields[7].trim();
                    String dueDateStr = fields[8].trim();
                    String returnDateStr = fields[9].trim();
                    String statusStr = fields[10].trim();
                    
                    // Parse dates first for duplicate checking
                    LocalDate loanDate = LocalDate.parse(loanDateStr);
                    LocalDate dueDate = LocalDate.parse(dueDateStr);
                    LocalDate returnDate = returnDateStr != null && !returnDateStr.isEmpty() ? 
                            LocalDate.parse(returnDateStr) : null;
                    
                    // Check for duplicates:
                    // 1. If Loan ID exists in database, skip (exact duplicate)
                    if (loanId != null) {
                        try {
                            Loan existingLoan = loanService.getLoanById(loanId);
                            if (existingLoan != null) {
                                // Loan already exists, skip
                                continue;
                            }
                        } catch (LoanNotFoundException e) {
                            // Loan doesn't exist by ID, continue checking
                        }
                    }
                    
                    // 2. Check if loan with same member, book, and loan date already exists
                    // This prevents duplicates even if IDs are different
                    boolean duplicateFound = false;
                    if (memberId != null && bookId != null) {
                        try {
                            // Check active loans
                            List<Loan> activeLoans = loanService.getActiveLoansByMember(memberId);
                            for (Loan existingLoan : activeLoans) {
                                if (existingLoan.getBookId().equals(bookId) && 
                                    existingLoan.getLoanDate().equals(loanDate)) {
                                    duplicateFound = true;
                                    break;
                                }
                            }
                            
                            // Also check closed/returned loans
                            if (!duplicateFound) {
                                List<Loan> closedLoans = loanService.getClosedLoansByMember(memberId);
                                for (Loan existingLoan : closedLoans) {
                                    if (existingLoan.getBookId().equals(bookId) && 
                                        existingLoan.getLoanDate().equals(loanDate)) {
                                        duplicateFound = true;
                                        break;
                                    }
                                }
                            }
                        } catch (MemberNotFoundException e) {
                            // Continue if error checking
                        }
                    }
                    
                    if (duplicateFound) {
                        // Skip this record, it's a duplicate
                        skippedCount++;
                        continue;
                    }
                    
                    // Validate member exists
                    if (memberId == null) {
                        errors.append("Line ").append(lineNumber).append(": Member ID is required\n");
                        continue;
                    }
                    
                    try {
                        memberService.getMemberById(memberId);
                    } catch (MemberNotFoundException e) {
                        errors.append("Line ").append(lineNumber).append(": Member ID ").append(memberId).append(" not found\n");
                        continue;
                    }
                    
                    // Validate book exists
                    if (bookId == null) {
                        errors.append("Line ").append(lineNumber).append(": Book ID is required\n");
                        continue;
                    }
                    
                    try {
                        bookService.getBookById(bookId);
                    } catch (BookNotFoundException e) {
                        errors.append("Line ").append(lineNumber).append(": Book ID ").append(bookId).append(" not found\n");
                        continue;
                    }
                    
                    // Parse status
                    LoanStatus status = LoanStatus.valueOf(statusStr);
                    
                    // Restore historical loan with original dates and status
                    loanService.restoreHistoricalLoan(memberId, bookId, 
                                                      loanDate, dueDate, returnDate, status);
                    
                    importedCount++;
                    
                } catch (BookNotFoundException | MemberNotFoundException e) {
                    errors.append("Line ").append(lineNumber).append(": ").append(e.getMessage()).append("\n");
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
        
        // Build comprehensive result message
        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("Import Summary:\n");
        resultMessage.append("✓ Imported: ").append(importedCount).append(" loans\n");
        if (skippedCount > 0) {
            resultMessage.append("⊘ Skipped (duplicates): ").append(skippedCount).append(" loans\n");
        }
        
        if (errors.length() > 0) {
            resultMessage.append("\n❌ Errors:\n").append(errors.toString());
            throw new RuntimeException(resultMessage.toString());
        }
        
        if (skippedCount > 0) {
            throw new RuntimeException(resultMessage.toString());
        }
        
        return importedCount;
    }
    
    /**
     * Import members from CSV file
     */
    public static int importMembers(IMemberService memberService, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Members");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return 0;
        }
        
        int importedCount = 0;
        int skippedCount = 0;
        StringBuilder errors = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Skip header
            int lineNumber = 1;
            String line;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = parseCsvLine(line);
                    if (fields.length < 5) {
                        errors.append("Line ").append(lineNumber).append(": Not enough fields\n");

                        continue;
                    }
                    
                    // Expected format: ID, Document Number, First Name, Last Name, Email, Phone, Address, Registration Date, Status
                    // Field 0: ID (optional - if exists, check for duplicates)
                    Integer memberId = null;
                    String memberIdField = fields[0].trim();
                    if (!memberIdField.isEmpty()) {
                        try {
                            memberId = Integer.valueOf(memberIdField);
                        } catch (NumberFormatException e) {
                            errors.append("Line ").append(lineNumber).append(": Invalid ID format: ").append(fields[0]).append("\n");
                            continue;
                        }
                    }
                    
                    String docNumber = fields[1].trim();
                    String firstName = fields[2].trim();
                    String lastName = fields[3].trim();
                    String email = fields[4].trim();
                    String phone = fields.length > 5 ? fields[5].trim() : null;
                    String address = fields.length > 6 ? fields[6].trim() : null;
                    // Registration Date is at index 7 (not used during import)
                    String status = fields.length > 8 ? fields[8].trim() : "ACTIVE";
                    
                    // Clean phone number - remove hyphens, spaces, and other non-digit characters
                    if (phone != null && !phone.isEmpty()) {
                        phone = phone.replaceAll("[^0-9]", "");
                    }
                    
                    // Clean document ID - remove any non-alphanumeric characters
                    if (docNumber != null && !docNumber.isEmpty()) {
                        docNumber = docNumber.replaceAll("[^a-zA-Z0-9]", "");
                    }
                    
                    // If ID exists, check if member with that ID already exists (skip duplicate)
                    if (memberId != null) {
                        try {
                            Member existingMember = memberService.getMemberById(memberId);
                            if (existingMember != null) {
                                // Member already exists, skip
                                skippedCount++;
                                continue;
                            }
                        } catch (MemberNotFoundException e) {
                            // Member doesn't exist, continue with creation
                        }
                    }
                    
                    // Check if member already exists by document number or email
                    try {
                        Member existingByDoc = memberService.getMemberByDocumentId(docNumber);
                        if (existingByDoc != null) {
                            errors.append("Line ").append(lineNumber).append(": Member with document ").append(docNumber).append(" already exists\n");
                            skippedCount++;
                            continue;
                        }
                    } catch (MemberNotFoundException e) {
                        // Member doesn't exist, continue
                    }
                    
                    try {
                        Member existingByEmail = memberService.getMemberByEmail(email);
                        if (existingByEmail != null) {
                            errors.append("Line ").append(lineNumber).append(": Member with email ").append(email).append(" already exists\n");
                            skippedCount++;
                            continue;
                        }
                    } catch (MemberNotFoundException e) {
                        // Member doesn't exist, continue
                    }
                    
                    // Register member - note the correct parameter order: firstName, lastName, documentId, email, phone, address
                    memberService.registerMember(firstName, lastName, docNumber, email, phone, address);
                    
                    // If the status in CSV is INACTIVE or SUSPENDED, update the member status
                    if (status != null && !status.trim().equalsIgnoreCase("ACTIVE")) {
                        Member member = memberService.getMemberByDocumentId(docNumber);
                        if (member != null && status.trim().equalsIgnoreCase("INACTIVE")) {
                            memberService.deactivateMember(member.getId());
                        }
                        // Note: SUSPENDED status would need a separate method if it exists
                    }
                    
                    importedCount++;
                    
                } catch (MemberAlreadyExistsException | MemberNotFoundException | ValidationException e) {
                    errors.append("Line ").append(lineNumber).append(": ").append(e.getMessage()).append("\n");
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage());
        }
        
        // Build comprehensive result message
        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("Import Summary:\n");
        resultMessage.append("✓ Imported: ").append(importedCount).append(" members\n");
        if (skippedCount > 0) {
            resultMessage.append("⊘ Skipped (duplicates): ").append(skippedCount).append(" members\n");
        }
        
        if (errors.length() > 0) {
            resultMessage.append("\n❌ Errors:\n").append(errors.toString());
            throw new RuntimeException(resultMessage.toString());
        }
        
        if (skippedCount > 0) {
            throw new RuntimeException(resultMessage.toString());
        }
        
        return importedCount;
    }
    
    /**
     * Result of a CSV import operation
     */
    public static class ImportResult {
        private int successCount = 0;
        private int skippedCount = 0;
        private final List<String> errors = new ArrayList<>();
        
        public void incrementSuccess() {
            successCount++;
        }
        
        public void incrementSkipped() {
            skippedCount++;
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getSkippedCount() {
            return skippedCount;
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append("Import Summary:\n");
            summary.append("✓ Imported: ").append(successCount).append("\n");
            if (skippedCount > 0) {
                summary.append("⊘ Skipped (duplicates): ").append(skippedCount).append("\n");
            }
            if (!errors.isEmpty()) {
                summary.append("❌ Errors: ").append(errors.size());
            }
            return summary.toString();
        }
    }
}
