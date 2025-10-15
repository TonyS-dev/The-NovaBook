package com.codeup.novabook.ui.view.dialog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.exception.BookNotAvailableException;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.service.IBookService;
import com.codeup.novabook.service.IConfigService;
import com.codeup.novabook.service.ILoanService;
import com.codeup.novabook.service.IMemberService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for creating new book loans.
 * Allows selecting a member, book, and due date with validation.
 */
public class LoanDialog extends Stage {
    
    private final ILoanService loanService;
    private final IMemberService memberService;
    private final IBookService bookService;
    private final IConfigService configService;
    
    private ComboBox<Member> memberCombo;
    private ComboBox<Book> bookCombo;
    private DatePicker dueDatePicker;
    private Label availableLabel;
    
    private boolean confirmed = false;
    private Loan resultLoan;
    
    public LoanDialog(ILoanService loanService, IMemberService memberService, 
                     IBookService bookService, IConfigService configService) {
        this.loanService = loanService;
        this.memberService = memberService;
        this.bookService = bookService;
        this.configService = configService;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Create New Loan");  // Removed emoji for compatibility
        setResizable(false);
        
        buildUI();
        loadData();
    }
    
    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        
        // Member selection
        Label memberLabel = new Label("Member *");
        memberCombo = new ComboBox<>();
        memberCombo.setPromptText("Select member");
        memberCombo.setPrefWidth(300);
        memberCombo.setButtonCell(new MemberListCell());
        memberCombo.setCellFactory(lv -> new MemberListCell());
        grid.add(memberLabel, 0, 0);
        grid.add(memberCombo, 1, 0);
        
        // Book selection
        Label bookLabel = new Label("Book *");
        bookCombo = new ComboBox<>();
        bookCombo.setPromptText("Select book");
        bookCombo.setPrefWidth(300);
        bookCombo.setButtonCell(new BookListCell());
        bookCombo.setCellFactory(lv -> new BookListCell());
        bookCombo.setOnAction(e -> updateAvailableInfo());
        grid.add(bookLabel, 0, 1);
        grid.add(bookCombo, 1, 1);
        
        // Available copies info
        availableLabel = new Label("");
        availableLabel.setStyle("-fx-font-size: 11; -fx-text-fill: gray;");
        grid.add(availableLabel, 1, 2);
        
        // Due date picker - use default loan duration from config
        Label dueDateLabel = new Label("Due Date *");
        dueDatePicker = new DatePicker();
        int defaultDuration = configService.getDefaultLoanDays();
        dueDatePicker.setValue(LocalDate.now().plusDays(defaultDuration));
        dueDatePicker.setPrefWidth(200);
        grid.add(dueDateLabel, 0, 3);
        grid.add(dueDatePicker, 1, 3);
        
        // Note
        Label noteLabel = new Label("* Required fields");
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        grid.add(noteLabel, 1, 4);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button createBtn = new Button("Create Loan");  // Removed emoji
        createBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        createBtn.setDefaultButton(true);
        createBtn.setOnAction(e -> handleCreate());
        
        Button cancelBtn = new Button("Cancel");  // Removed emoji
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(createBtn, cancelBtn);
        grid.add(buttonBox, 1, 5);
        
        Scene scene = new Scene(grid);
        setScene(scene);
    }
    
    private void loadData() {
        try {
            // Load active members
            List<Member> members = memberService.getActiveMembers();
            memberCombo.getItems().setAll(members);
            
            // Load active books
            List<Book> books = bookService.getActiveBooks();
            bookCombo.getItems().setAll(books);
            
        } catch (Exception e) {
            showError("Error loading data: " + e.getMessage());
        }
    }
    
    private void updateAvailableInfo() {
        Book selected = bookCombo.getValue();
        if (selected != null) {
            int available = selected.getAvailableCopies();
            if (available > 0) {
                availableLabel.setText("[OK] Available copies: " + available);  // Replaced emoji with text
                availableLabel.setStyle("-fx-font-size: 11; -fx-text-fill: green;");
            } else {
                availableLabel.setText("[X] No copies available");  // Replaced emoji with text
                availableLabel.setStyle("-fx-font-size: 11; -fx-text-fill: red;");
            }
        } else {
            availableLabel.setText("");
        }
    }
    
    private void handleCreate() {
        try {
            // Validate selections
            Member member = memberCombo.getValue();
            Book book = bookCombo.getValue();
            LocalDate dueDate = dueDatePicker.getValue();
            
            if (member == null) {
                showError("Please select a member");
                memberCombo.requestFocus();
                return;
            }
            
            if (book == null) {
                showError("Please select a book");
                bookCombo.requestFocus();
                return;
            }
            
            if (dueDate == null) {
                showError("Please select a due date");
                dueDatePicker.requestFocus();
                return;
            }
            
            // Validate due date is in the future
            if (!dueDate.isAfter(LocalDate.now())) {
                showError("Due date must be in the future");
                dueDatePicker.requestFocus();
                return;
            }
            
            // Check book availability
            if (book.getAvailableCopies() <= 0) {
                showError("This book has no available copies");
                return;
            }
            
            // Create loan via service
            resultLoan = loanService.createLoan(member.getId(), book.getId(), dueDate);
            
            confirmed = true;
            close();
            
        } catch (BookNotAvailableException | BookNotFoundException | MemberNotFoundException | ValidationException e) {
            showError("Error creating loan: " + e.getMessage());
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
    
    public Optional<Loan> showAndWaitResult() {
        showAndWait();
        return confirmed ? Optional.ofNullable(resultLoan) : Optional.empty();
    }
    
    // Custom cell renderer for Member ComboBox
    private static class MemberListCell extends ListCell<Member> {
        @Override
        protected void updateItem(Member member, boolean empty) {
            super.updateItem(member, empty);
            if (empty || member == null) {
                setText(null);
            } else {
                setText(member.getFullName() + " (" + member.getDocumentId() + ")");
            }
        }
    }
    
    // Custom cell renderer for Book ComboBox
    private static class BookListCell extends ListCell<Book> {
        @Override
        protected void updateItem(Book book, boolean empty) {
            super.updateItem(book, empty);
            if (empty || book == null) {
                setText(null);
            } else {
                setText(book.getTitle() + " - " + book.getAuthor() + 
                       " [Available: " + book.getAvailableCopies() + "]");
            }
        }
    }
}
