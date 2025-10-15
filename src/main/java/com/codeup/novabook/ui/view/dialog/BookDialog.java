package com.codeup.novabook.ui.view.dialog;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.BookCategory;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.service.IBookService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

import com.codeup.novabook.exception.BookNotFoundException;

/**
 * Dialog for creating or editing books in the library system.
 * Provides form validation for ISBN, title, author, and inventory fields.
 */
public class BookDialog extends Stage {
    
    private final IBookService bookService;
    private final Book existingBook; // null for new book, non-null for edit
    
    private TextField isbnField;
    private TextField titleField;
    private TextField authorField;
    private ComboBox<BookCategory> categoryCombo;
    private Spinner<Integer> totalCopiesSpinner;
    private TextField referencePriceField;
    
    private boolean confirmed = false;
    private Book resultBook;
    
    /**
     * Constructor for adding a new book
     */
    public BookDialog(IBookService bookService) {
        this(bookService, null);
    }
    
    /**
     * Constructor for editing an existing book
     */
    public BookDialog(IBookService bookService, Book book) {
        this.bookService = bookService;
        this.existingBook = book;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle(book == null ? "📚 Add New Book" : "✏️ Edit Book");
        setResizable(false);
        
        buildUI();
    }
    
    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        
        // ISBN field
        Label isbnLabel = new Label("ISBN *");
        isbnField = new TextField();
        isbnField.setPromptText("978-3-16-148410-0");
        isbnField.setPrefWidth(300);
        isbnField.setDisable(existingBook != null); // ISBN cannot be changed when editing
        grid.add(isbnLabel, 0, 0);
        grid.add(isbnField, 1, 0);
        
        // Title field
        Label titleLabel = new Label("Title *");
        titleField = new TextField();
        titleField.setPromptText("Book title");
        titleField.setPrefWidth(300);
        grid.add(titleLabel, 0, 1);
        grid.add(titleField, 1, 1);
        
        // Author field
        Label authorLabel = new Label("Author *");
        authorField = new TextField();
        authorField.setPromptText("Author name");
        grid.add(authorLabel, 0, 2);
        grid.add(authorField, 1, 2);
        
        // Category ComboBox with enum values
        Label categoryLabel = new Label("Category");
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(BookCategory.values());
        categoryCombo.setPromptText("Select a category...");
        categoryCombo.setPrefWidth(300);
        
        // Set up StringConverter to display friendly names
        categoryCombo.setConverter(new StringConverter<BookCategory>() {
            @Override
            public String toString(BookCategory category) {
                return category != null ? category.getDisplayName() : "";
            }
            
            @Override
            public BookCategory fromString(String string) {
                return BookCategory.fromDisplayName(string);
            }
        });
        
        grid.add(categoryLabel, 0, 3);
        grid.add(categoryCombo, 1, 3);
        
        // Total copies spinner
        Label copiesLabel = new Label("Total Copies *");
        totalCopiesSpinner = new Spinner<>(1, 1000, 1);
        totalCopiesSpinner.setEditable(true);
        totalCopiesSpinner.setPrefWidth(100);
        grid.add(copiesLabel, 0, 4);
        grid.add(totalCopiesSpinner, 1, 4);
        
        // Reference price field
        Label priceLabel = new Label("Reference Price");
        referencePriceField = new TextField();
        referencePriceField.setPromptText("0.00");
        referencePriceField.setPrefWidth(150);
        grid.add(priceLabel, 0, 5);
        grid.add(referencePriceField, 1, 5);
        
        // Required fields note
        Label noteLabel = new Label("* Required fields");
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        grid.add(noteLabel, 1, 6);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button saveBtn = new Button(existingBook == null ? "➕ Add" : "💾 Save");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> handleSave());
        
        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        grid.add(buttonBox, 1, 7);
        
        // Populate fields if editing
        if (existingBook != null) {
            populateFields();
        }
        
        Scene scene = new Scene(grid);
        setScene(scene);
    }
    
    private void populateFields() {
        isbnField.setText(existingBook.getIsbn());
        titleField.setText(existingBook.getTitle());
        authorField.setText(existingBook.getAuthor());
        
        // Set the selected category
        if (existingBook.getCategory() != null) {
            categoryCombo.setValue(existingBook.getCategory());
        }
        
        totalCopiesSpinner.getValueFactory().setValue(existingBook.getTotalCopies());
        
        if (existingBook.getReferencePrice() != null) {
            referencePriceField.setText(existingBook.getReferencePrice().toString());
        }
    }
    
    private void handleSave() {
        try {
            // Validate required fields
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            BookCategory category = categoryCombo.getValue(); // Get selected category
            Integer totalCopies = totalCopiesSpinner.getValue();
            
            if (isbn.isEmpty()) {
                showError("ISBN is required");
                isbnField.requestFocus();
                return;
            }
            
            if (title.isEmpty()) {
                showError("Title is required");
                titleField.requestFocus();
                return;
            }
            
            if (author.isEmpty()) {
                showError("Author is required");
                authorField.requestFocus();
                return;
            }
            
            // Additional ISBN validation (basic format)
            if (!isbn.matches("^[0-9\\-]+$")) {
                showError("ISBN must contain only numbers and hyphens");
                isbnField.requestFocus();
                return;
            }
            
            // Check ISBN uniqueness for new books
            if (existingBook == null && bookService.isbnExists(isbn)) {
                showError("A book with this ISBN already exists");
                isbnField.requestFocus();
                return;
            }
            
            // Check ISBN uniqueness for edited books (different book)
            if (existingBook != null && bookService.isbnExistsForOtherBook(isbn, existingBook.getId())) {
                showError("Another book with this ISBN already exists");
                isbnField.requestFocus();
                return;
            }
            
            // Parse and validate reference price
            java.math.BigDecimal referencePrice = null;
            String priceText = referencePriceField.getText().trim();
            if (!priceText.isEmpty()) {
                try {
                    referencePrice = new java.math.BigDecimal(priceText);
                    if (referencePrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
                        showError("Price cannot be negative");
                        referencePriceField.requestFocus();
                        return;
                    }
                    // Database constraint: DECIMAL(10, 2) - max value is 99,999,999.99
                    java.math.BigDecimal maxPrice = new java.math.BigDecimal("99999999.99");
                    if (referencePrice.compareTo(maxPrice) > 0) {
                        showError("Price is too large. Maximum allowed: 99,999,999.99");
                        referencePriceField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid price format. Use numbers only (e.g., 45000.00)");
                    referencePriceField.requestFocus();
                    return;
                }
            }
            
            // Save via service (using method signatures from IBookService)
            // Convert BookCategory to String for database storage
            String categoryString = (category != null) ? category.toDatabaseValue() : null;
            
            if (existingBook == null) {
                // registerBook(String title, String author, String isbn, String category, Integer stock, BigDecimal referencePrice)
                resultBook = bookService.registerBook(title, author, isbn, 
                        categoryString, totalCopies, referencePrice);
            } else {
                // updateBook(Integer bookId, String title, String author, String isbn, String category, Integer stock, BigDecimal referencePrice)
                resultBook = bookService.updateBook(existingBook.getId(), title, author, isbn,
                        categoryString, totalCopies, referencePrice);
            }
            
            confirmed = true;
            close();
            
        } catch (ValidationException e) {
            showError("Validation error: " + e.getMessage());
        } catch (BookNotFoundException e) {
            showError("Error saving book: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(this);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows the dialog and waits for user action
     * @return Optional containing the saved book if confirmed, empty otherwise
     */
    public Optional<Book> showAndWaitResult() {
        showAndWait();
        return confirmed ? Optional.ofNullable(resultBook) : Optional.empty();
    }
}
