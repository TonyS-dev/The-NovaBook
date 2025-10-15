package com.codeup.novabook.ui.view.dialog;

import java.time.LocalDate;
import java.util.Optional;

import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.exception.LoanAlreadyClosedException;
import com.codeup.novabook.exception.LoanNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.service.ILoanService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for extending loan due dates.
 * Allows extending the due date for active loans.
 */
public class ExtendLoanDialog extends Stage {
    
    private final ILoanService loanService;
    private final Loan loan;
    
    private DatePicker newDueDatePicker;
    private Label currentDueDateLabel;
    
    private boolean confirmed = false;
    private Loan resultLoan;
    
    public ExtendLoanDialog(ILoanService loanService, Loan loan) {
        this.loanService = loanService;
        this.loan = loan;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("📅 Extend Loan");
        setResizable(false);
        
        buildUI();
    }
    
    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        
        // Title
        Label titleLabel = new Label("📅 Extend Loan Due Date");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        grid.add(titleLabel, 0, 0, 2, 1);
        
        // Loan ID
        Label idLabel = new Label("Loan ID:");
        Label idValue = new Label("#" + loan.getId());
        idValue.setStyle("-fx-font-weight: bold;");
        grid.add(idLabel, 0, 1);
        grid.add(idValue, 1, 1);
        
        // Current due date
        Label currentLabel = new Label("Current Due Date:");
        currentDueDateLabel = new Label(loan.getExpectedReturnDate().toString());
        currentDueDateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22;");
        grid.add(currentLabel, 0, 2);
        grid.add(currentDueDateLabel, 1, 2);
        
        // New due date picker
        Label newLabel = new Label("New Due Date *");
        newDueDatePicker = new DatePicker();
        newDueDatePicker.setValue(loan.getExpectedReturnDate().plusDays(7)); // Default: +7 days
        newDueDatePicker.setPrefWidth(200);
        grid.add(newLabel, 0, 3);
        grid.add(newDueDatePicker, 1, 3);
        
        // Info message
        Label infoLabel = new Label("ℹ️ New date must be after current due date");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray; -fx-font-size: 11;");
        grid.add(infoLabel, 0, 4, 2, 1);
        
        // Separator
        Separator separator = new Separator();
        grid.add(separator, 0, 5, 2, 1);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button extendBtn = new Button("✅ Extend Loan");
        extendBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        extendBtn.setDefaultButton(true);
        extendBtn.setOnAction(e -> handleExtend());
        
        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(extendBtn, cancelBtn);
        grid.add(buttonBox, 0, 6, 2, 1);
        
        Scene scene = new Scene(grid);
        setScene(scene);
    }
    
    private void handleExtend() {
        try {
            LocalDate newDueDate = newDueDatePicker.getValue();
            
            if (newDueDate == null) {
                showError("Please select a new due date");
                newDueDatePicker.requestFocus();
                return;
            }
            
            // Validate new date is after current due date
            if (!newDueDate.isAfter(loan.getExpectedReturnDate())) {
                showError("New due date must be after current due date (" + 
                         loan.getExpectedReturnDate() + ")");
                newDueDatePicker.requestFocus();
                return;
            }
            
            // Validate new date is in the future
            if (!newDueDate.isAfter(LocalDate.now())) {
                showError("New due date must be in the future");
                newDueDatePicker.requestFocus();
                return;
            }
            
            // Confirm extension
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(this);
            confirm.setTitle("Confirm Extension");
            confirm.setHeaderText("Extend loan due date");
            confirm.setContentText("Current: " + loan.getExpectedReturnDate() + 
                                  "\nNew: " + newDueDate + 
                                  "\n\nConfirm extension?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
            
            // Extend loan via service
            resultLoan = loanService.extendLoan(loan.getId(), newDueDate);
            
            confirmed = true;
            close();
            
        } catch (LoanAlreadyClosedException | LoanNotFoundException | ValidationException e) {
            showError("Error extending loan: " + e.getMessage());
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
}
