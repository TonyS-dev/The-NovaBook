package com.codeup.novabook.ui.view.dialog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.exception.LoanAlreadyClosedException;
import com.codeup.novabook.exception.LoanNotFoundException;
import com.codeup.novabook.service.IConfigService;
import com.codeup.novabook.service.ILoanService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for returning loans with automatic fine calculation.
 * Displays loan details and calculates fines for overdue returns.
 */
public class ReturnLoanDialog extends Stage {
    
    private final ILoanService loanService;
    private final IConfigService configService;
    private final Loan loan;
    
    private TextField fineRateField;
    private Label daysOverdueLabel;
    private Label calculatedFineLabel;
    
    private boolean confirmed = false;
    private Loan resultLoan;
    
    public ReturnLoanDialog(ILoanService loanService, IConfigService configService, Loan loan) {
        this.loanService = loanService;
        this.configService = configService;
        this.loan = loan;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("📥 Return Loan");
        setResizable(false);
        
        buildUI();
        calculateFine();
    }
    
    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        
        // Loan info header
        Label titleLabel = new Label("📥 Return Loan Details");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        grid.add(titleLabel, 0, 0, 2, 1);
        
        // Loan ID
        Label idLabel = new Label("Loan ID:");
        Label idValue = new Label("#" + loan.getId());
        idValue.setStyle("-fx-font-weight: bold;");
        grid.add(idLabel, 0, 1);
        grid.add(idValue, 1, 1);
        
        // Due date
        Label dueDateLabel = new Label("Due Date:");
        Label dueDateValue = new Label(loan.getExpectedReturnDate().toString());
        grid.add(dueDateLabel, 0, 2);
        grid.add(dueDateValue, 1, 2);
        
        // Days overdue
        Label overdueLabel = new Label("Days Overdue:");
        daysOverdueLabel = new Label("");
        grid.add(overdueLabel, 0, 3);
        grid.add(daysOverdueLabel, 1, 3);
        
        // Fine rate field - get from config
        Label fineRateLabel = new Label("Fine Rate (per day):");
        BigDecimal dailyFine = configService.getDailyFineAmount();
        fineRateField = new TextField(dailyFine.toString());
        fineRateField.setPrefWidth(100);
        fineRateField.textProperty().addListener((obs, old, val) -> calculateFine());
        grid.add(fineRateLabel, 0, 4);
        grid.add(fineRateField, 1, 4);
        
        // Calculated fine
        Label calculatedLabel = new Label("Total Fine:");
        calculatedFineLabel = new Label("");
        calculatedFineLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        grid.add(calculatedLabel, 0, 5);
        grid.add(calculatedFineLabel, 1, 5);
        
        // Separator
        Separator separator = new Separator();
        grid.add(separator, 0, 6, 2, 1);
        
        // Warning message if overdue
        Label warningLabel = new Label();
        if (isOverdue()) {
            warningLabel.setText("⚠️ This loan is overdue. Fine will be applied.");
            warningLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic;");
        } else {
            warningLabel.setText("✅ Return on time. No fine.");
            warningLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-style: italic;");
        }
        grid.add(warningLabel, 0, 7, 2, 1);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button returnBtn = new Button("✅ Confirm Return");
        returnBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        returnBtn.setDefaultButton(true);
        returnBtn.setOnAction(e -> handleReturn());
        
        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(returnBtn, cancelBtn);
        grid.add(buttonBox, 0, 8, 2, 1);
        
        Scene scene = new Scene(grid);
        setScene(scene);
    }
    
    private boolean isOverdue() {
        return LocalDate.now().isAfter(loan.getExpectedReturnDate());
    }
    
    private void calculateFine() {
        try {
            LocalDate dueDate = loan.getExpectedReturnDate();
            LocalDate today = LocalDate.now();
            
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
            
            if (daysOverdue > 0) {
                daysOverdueLabel.setText(daysOverdue + " days");
                daysOverdueLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                
                BigDecimal fineRate = new BigDecimal(fineRateField.getText().trim());
                BigDecimal totalFine = fineRate.multiply(new BigDecimal(daysOverdue));
                
                calculatedFineLabel.setText("$" + totalFine.setScale(2, RoundingMode.HALF_UP));
                calculatedFineLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: red;");
            } else {
                daysOverdueLabel.setText("0 days (on time)");
                daysOverdueLabel.setStyle("-fx-text-fill: green;");
                
                calculatedFineLabel.setText("$0.00");
                calculatedFineLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: green;");
            }
            
        } catch (NumberFormatException e) {
            calculatedFineLabel.setText("Invalid rate");
            calculatedFineLabel.setStyle("-fx-text-fill: gray;");
        }
    }
    
    private void handleReturn() {
        try {
            // Validate fine rate
            BigDecimal fineRate;
            try {
                fineRate = new BigDecimal(fineRateField.getText().trim());
                if (fineRate.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Fine rate cannot be negative");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Invalid fine rate format");
                fineRateField.requestFocus();
                return;
            }
            
            // Confirm if there's a fine
            long daysOverdue = ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), LocalDate.now());
            if (daysOverdue > 0) {
                BigDecimal totalFine = fineRate.multiply(new BigDecimal(daysOverdue));
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.initOwner(this);
                confirm.setTitle("Confirm Fine");
                confirm.setHeaderText("This loan is " + daysOverdue + " days overdue");
                confirm.setContentText("A fine of $" + totalFine.setScale(2, RoundingMode.HALF_UP) + 
                                      " will be applied.\n\nProceed with return?");
                
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }
            
            // Return loan via service
            resultLoan = loanService.returnLoan(loan.getId(), fineRate);
            
            confirmed = true;
            close();
            
        } catch (LoanAlreadyClosedException | LoanNotFoundException e) {
            showError("Error returning loan: " + e.getMessage());
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
