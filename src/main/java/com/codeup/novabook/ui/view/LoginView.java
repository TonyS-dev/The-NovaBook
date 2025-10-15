package com.codeup.novabook.ui.view;

import java.util.Optional;

import com.codeup.novabook.domain.User;
import com.codeup.novabook.exception.AuthenticationException;
import com.codeup.novabook.exception.UserAlreadyExistsException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.ui.ServiceContainer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Login/Register View for NovaBook Library System
 * 
 * <p>This view handles user authentication and registration with:</p>
 * <ul>
 *   <li>Login/Register mode toggle</li>
 *   <li>Test Mode with 3 pre-configured users</li>
 *   <li>Form validation</li>
 *   <li>Role-based navigation to Dashboard</li>
 * </ul>
 * 
 * <p><strong>Test Mode Users:</strong></p>
 * <ul>
 *   <li>ADMIN: admin@mail.com / Admin123!</li>
 *   <li>ASSISTANT: asistente@mail.com / Asist123!</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class LoginView extends BaseView {
    private TextField emailField;
    private PasswordField passwordField;
    private TextField nameField;
    private TextField phoneField;
    private Button loginButton;
    private Button registerButton;
    private Button toggleButton;
    private Label titleLabel;
    private GridPane formPane;
    private boolean isLoginMode = true;
    
    /**
     * Constructs a new LoginView for authentication and registration.
     * @param services the service container
     */
    public LoginView(ServiceContainer services) {
        super(services);
    }
    
    @Override
    protected void buildContent() {
        createTitle();
        createForm();
        createButtons();
        updateFormMode();
    }
    
    private void createTitle() {
        titleLabel = new Label("📚 NovaBook - Library System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #1976D2;");
        root.getChildren().add(titleLabel);
        
        Label subtitleLabel = new Label("Login");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleLabel.setStyle("-fx-text-fill: #666;");
        root.getChildren().add(subtitleLabel);
    }
    
    private void createForm() {
        formPane = new GridPane();
        formPane.setHgap(10);
        formPane.setVgap(15);
        formPane.setAlignment(Pos.CENTER);
        formPane.setPadding(new Insets(20));
        formPane.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // Email field
        formPane.add(new Label("Email:"), 0, 0);
        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefWidth(250);
        formPane.add(emailField, 1, 0);
        
        // Password field
        formPane.add(new Label("Password:"), 0, 1);
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        formPane.add(passwordField, 1, 1);
        
        // Name field (for registration)
        formPane.add(new Label("Name:"), 0, 2);
        nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        formPane.add(nameField, 1, 2);
        
        // Phone field (for registration)
        formPane.add(new Label("Phone:"), 0, 3);
        phoneField = new TextField();
        phoneField.setPromptText("Enter your phone number");
        formPane.add(phoneField, 1, 3);
        
        root.getChildren().add(formPane);
    }
    
    private void createButtons() {
        loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        loginButton.setPrefWidth(120);
        loginButton.setOnAction(e -> handleLogin());
        
        registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
        registerButton.setPrefWidth(120);
        registerButton.setOnAction(e -> handleRegister());
        
        toggleButton = new Button("Need an account? Register");
        toggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666; -fx-underline: true;");
        toggleButton.setOnAction(e -> toggleMode());
        
        // Test mode button for easy navigation
        Button testButton = new Button("Test Mode");
        testButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 12px;");
        testButton.setPrefWidth(100);
        testButton.setOnAction(e -> handleTestMode());
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton);
        
        HBox testBox = new HBox(10);
        testBox.setAlignment(Pos.CENTER);
        testBox.getChildren().addAll(testButton);
        
        root.getChildren().addAll(buttonBox, toggleButton, testBox);
    }
    
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill all required fields");
            return;
        }
        
        try {
            Optional<User> userOpt = services.getUserService().authenticate(email, password);
            if (userOpt.isPresent()) {
                services.setCurrentUser(userOpt.get());
                showSuccess("Login successful!");
                
                // Navigate to dashboard based on role
                DashboardView dashboard = new DashboardView(services);
                dashboard.show(stage);
            } else {
                showError("Invalid email or password");
            }
        } catch (AuthenticationException e) {
            showError("Error during login: " + e.getMessage());
        }
    }
    
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneField.getText().trim();
        
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            showError("Please fill all fields");
            return;
        }
        
        try {
            services.getUserService().register(name, email, password, phone);
            showSuccess("Registration successful! You can now login.");
            toggleMode(); // Switch to login mode
            clearFields();
        } catch (UserAlreadyExistsException | ValidationException e) {
            showError("Registration failed: " + e.getMessage());
        }
    }
    
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateFormMode();
        clearFields();
    }
    
    private void updateFormMode() {
        if (isLoginMode) {
            titleLabel.setText("📚 NovaBook - Library System");
            nameField.setVisible(false);
            phoneField.setVisible(false);
            formPane.getChildren().get(4).setVisible(false); // Name label
            formPane.getChildren().get(6).setVisible(false); // Phone label
            loginButton.setVisible(true);
            registerButton.setVisible(false);
            toggleButton.setText("Need an account? Register");
        } else {
            titleLabel.setText("📚 NovaBook - Register");
            nameField.setVisible(true);
            phoneField.setVisible(true);
            formPane.getChildren().get(4).setVisible(true); // Name label
            formPane.getChildren().get(6).setVisible(true); // Phone label
            loginButton.setVisible(false);
            registerButton.setVisible(true);
            toggleButton.setText("Already have an account? Login");
        }
    }
    
    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        nameField.clear();
        phoneField.clear();
    }
    
    private void handleTestMode() {
        // Create choice dialog with test users
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Admin", "Admin", "Assistant");
        dialog.setTitle("Test Mode - Quick Login");
        dialog.setHeaderText("🧪 Test Mode");
        dialog.setContentText("Select a user to login:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selectedRole = result.get();
            String email = "";
            String password = "";
            
            switch (selectedRole) {
                case "Admin" -> {
                    email = "admin@mail.com";
                    password = "Admin123!";
                }
                case "Assistant" -> {
                    email = "asistente@mail.com";
                    password = "Asist123!";
                }
            }
            
            try {
                Optional<User> userOpt = services.getUserService().authenticate(email, password);
                if (userOpt.isPresent()) {
                    services.setCurrentUser(userOpt.get());
                    showSuccess("Test mode activated - " + selectedRole + " access granted!");
                    
                    // Navigate to dashboard
                    DashboardView dashboard = new DashboardView(services);
                    dashboard.show(stage);
                } else {
                    showError("""
                              Test mode failed - User not found in database.
                              
                              Please ensure the database has been initialized with seed data.""");
                }
            } catch (AuthenticationException e) {
                showError("Test mode error: " + e.getMessage());
            }
        }
    }
    
    @Override
    protected String getTitle() {
        return "NovaBook - Library Management System";
    }
}