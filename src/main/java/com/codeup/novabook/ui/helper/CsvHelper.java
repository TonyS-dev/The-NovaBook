package com.codeup.novabook.ui.helper;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.service.IBookService;
import com.codeup.novabook.service.IMemberService;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
            
            // Write header
            writer.write("ISBN,Title,Author,Category,Total Copies,Available Copies,Reference Price,Active");
            writer.newLine();
            
            // Write data
            for (Book book : books) {
                writer.write(escapeCsv(book.getIsbn()));
                writer.write(",");
                writer.write(escapeCsv(book.getTitle()));
                writer.write(",");
                writer.write(escapeCsv(book.getAuthor()));
                writer.write(",");
                writer.write(escapeCsv(book.getCategory() != null ? book.getCategory() : ""));
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
            
            // Write header
            writer.write("Loan ID,Member Name,Member Document,Book Title,Book ISBN,Due Date,Days Overdue,Status");
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
                    writer.write(escapeCsv(member.getFullName()));
                    writer.write(",");
                    writer.write(escapeCsv(member.getDocumentId()));
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
                } catch (Exception e) {
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
                    
                    if (fields.length < 4) {
                        result.addError("Line " + lineNumber + ": Insufficient fields (need ISBN, Title, Author, Category minimum)");
                        continue;
                    }
                    
                    String isbn = fields[0].trim();
                    String title = fields[1].trim();
                    String author = fields[2].trim();
                    String category = fields.length > 3 ? fields[3].trim() : null;
                    int stock = fields.length > 4 && !fields[4].trim().isEmpty() ? 
                               Integer.parseInt(fields[4].trim()) : 1;
                    
                    // Parse reference price (field 6, index 5)
                    java.math.BigDecimal referencePrice = null;
                    if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                        try {
                            referencePrice = new java.math.BigDecimal(fields[5].trim());
                        } catch (NumberFormatException e) {
                            result.addError("Line " + lineNumber + ": Invalid price format: " + fields[5]);
                            continue;
                        }
                    }
                    
                    // Validate required fields
                    if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                        result.addError("Line " + lineNumber + ": ISBN, Title, and Author are required");
                        continue;
                    }
                    
                    // Check if book already exists
                    if (bookService.isbnExists(isbn)) {
                        result.addError("Line " + lineNumber + ": Book with ISBN " + isbn + " already exists");
                        continue;
                    }
                    
                    // Register book with reference price
                    bookService.registerBook(title, author, isbn, category, stock, referencePrice);
                    result.incrementSuccess();
                    
                } catch (NumberFormatException e) {
                    result.addError("Line " + lineNumber + ": Invalid number format");
                } catch (Exception e) {
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
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * Result of a CSV import operation
     */
    public static class ImportResult {
        private int successCount = 0;
        private final List<String> errors = new ArrayList<>();
        
        public void incrementSuccess() {
            successCount++;
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getSummary() {
            return String.format("Import completed: %d successful, %d errors", 
                               successCount, errors.size());
        }
    }
}
