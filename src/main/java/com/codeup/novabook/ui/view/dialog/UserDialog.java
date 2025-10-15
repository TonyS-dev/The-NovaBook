package com.codeup.novabook.ui.view.dialog;

import com.codeup.novabook.domain.UserRole;
import com.codeup.novabook.service.IUserService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for creating system users (ADMIN only feature).
 * Allows creating ADMIN or ASSISTANT users with password validation.
 */
public class UserDialog extends Stage {
    
    private final IUserService userService;
    
    private TextField nameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField phoneField;
    private ComboBox<UserRole> roleCombo;
    
    private boolean confirmed = false;
    
    public UserDialog(IUserService userService) {
        this.userService = userService;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("👤 Create System User");
        setResizable(false);
        
        buildUI();
    }
    
    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        
        // Title
        Label titleLabel = new Label("👤 Create New System User");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        grid.add(titleLabel, 0, 0, 2, 1);
        
        // Name field
        Label nameLabel = new Label("Name *");
        nameField = new TextField();
        nameField.setPromptText("Full name");
        nameField.setPrefWidth(300);
        grid.add(nameLabel, 0, 1);
        grid.add(nameField, 1, 1);
        
        // Email field
        Label emailLabel = new Label("Email *");
        emailField = new TextField();
        emailField.setPromptText("user@mail.com");
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 1, 2);
        
        // Phone field
        Label phoneLabel = new Label("Phone");
        phoneField = new TextField();
        phoneField.setPromptText("3001234567");
        grid.add(phoneLabel, 0, 3);
        grid.add(phoneField, 1, 3);
        
        // Role selection
        Label roleLabel = new Label("Role *");
        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(UserRole.ADMIN, UserRole.ASSISTANT);
        roleCombo.setValue(UserRole.ASSISTANT); // Default
        roleCombo.setPrefWidth(200);
        grid.add(roleLabel, 0, 4);
        grid.add(roleCombo, 1, 4);
        
        // Password field
        Label passwordLabel = new Label("Password *");
        passwordField = new PasswordField();
        passwordField.setPromptText("Min 8 characters");
        grid.add(passwordLabel, 0, 5);
        grid.add(passwordField, 1, 5);
        
        // Confirm password field
        Label confirmLabel = new Label("Confirm Password *");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");
        grid.add(confirmLabel, 0, 6);
        grid.add(confirmPasswordField, 1, 6);
        
        // Password requirements note
        Label reqLabel = new Label("Password must be at least 8 characters");
        reqLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray; -fx-font-size: 11;");
        grid.add(reqLabel, 1, 7);
        
        // Required fields note
        Label noteLabel = new Label("* Required fields");
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        grid.add(noteLabel, 1, 8);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button createBtn = new Button("✅ Create User");
        createBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        createBtn.setDefaultButton(true);
        createBtn.setOnAction(e -> handleCreate());
        
        Button cancelBtn = new Button("❌ Cancel");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(createBtn, cancelBtn);
        grid.add(buttonBox, 1, 9);
        
        Scene scene = new Scene(grid);
        setScene(scene);
    }
    
    private void handleCreate() {
        try {
            // Validate required fields
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            UserRole role = roleCombo.getValue();
            
            if (name.isEmpty()) {
                showError("Name is required");
                nameField.requestFocus();
                return;
            }
            
            if (email.isEmpty()) {
                showError("Email is required");
                emailField.requestFocus();
                return;
            }
            
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showError("Invalid email format");
                emailField.requestFocus();
                return;
            }
            
            if (password.isEmpty()) {
                showError("Password is required");
                passwordField.requestFocus();
                return;
            }
            
            if (password.length() < 8) {
                showError("Password must be at least 8 characters");
                passwordField.requestFocus();
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                confirmPasswordField.requestFocus();
                return;
            }
            
            if (role == null) {
                showError("Please select a role");
                roleCombo.requestFocus();
                return;
            }
            
            // Check if email already exists
            if (userService.userExists(email)) {
                showError("A user with this email already exists");
                emailField.requestFocus();
                return;
            }
            
            // Create user via service
            // adminRegister(String name, String email, String password, String phone, String role, String accessLevel)
            userService.adminRegister(name, email, password, phone, 
                                     role.name(), "READ_WRITE"); // Default access level
            
            confirmed = true;
            close();
            
        } catch (Exception e) {
            showError("Error creating user: " + e.getMessage());
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
    
    public boolean showAndWaitConfirmation() {
        showAndWait();
        return confirmed;
    }
}
