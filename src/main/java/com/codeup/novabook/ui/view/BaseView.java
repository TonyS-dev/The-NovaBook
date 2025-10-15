package com.codeup.novabook.ui.view;

import com.codeup.novabook.ui.ServiceContainer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Base View Class - Template Method Pattern
 * Provides common functionality for all views in the NovaBook application.
 * 
 * <p>This class implements the Template Method pattern and provides:</p>
 * <ul>
 *   <li>Common layout initialization</li>
 *   <li>Alert dialogs (info, success, warning, error)</li>
 *   <li>Confirmation dialogs</li>
 *   <li>Service container access</li>
 *   <li>Stage management</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public abstract class BaseView {
    /**
     * Service container for dependency injection and service access.
     */
    protected final ServiceContainer services;
    
    /**
     * The JavaFX stage associated with this view.
     */
    protected Stage stage;
    
    /**
     * The root VBox layout for the view.
     */
    protected VBox root;
    
    /**
     * Constructs a new BaseView with the given service container.
     * @param services the service container
     */
    protected BaseView(ServiceContainer services) {
        this.services = services;
        initializeLayout();
    }
    
    private void initializeLayout() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
    }
    
    /**
     * Builds the content of the view. Must be implemented by subclasses.
     */
    protected abstract void buildContent();
    
    /**
     * Shows the view in the given JavaFX stage.
     * @param stage the JavaFX stage
     */
    public void show(Stage stage) {
        this.stage = stage;
        buildContent();
        
        Scene scene = new Scene(root, 1200, 800);  // Increased from 800x600
        stage.setScene(scene);
        stage.setTitle(getTitle());
        stage.setResizable(true);  // Changed from false - allow resizing
        stage.setMinWidth(800);    // Set minimum width
        stage.setMinHeight(600);   // Set minimum height
        stage.show();
    }
    
    /**
     * Returns the title of the view. Must be implemented by subclasses.
     * @return the view title
     */
    protected abstract String getTitle();
    
    /**
     * Displays an alert dialog of the specified type.
     * @param type the alert type
     * @param title the dialog title
     * @param message the message to display
     */
    protected void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Displays an error message dialog.
     * @param message the error message
     */
    protected void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", message);
    }
    
    /**
     * Displays a success message dialog.
     * @param message the success message
     */
    protected void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
    }
    
    /**
     * Displays a warning message dialog.
     * @param message the warning message
     */
    protected void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning", message);
    }
    
    /**
     * Shows a confirmation dialog with Yes/No buttons.
     * 
     * <p>Use this before destructive operations like delete, deactivate, etc.</p>
     * 
     * @param title the dialog title
     * @param message the confirmation message to display
     * @return {@code true} if user clicked OK/Yes, {@code false} if user clicked Cancel/No
     */
    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Shows a destructive confirmation dialog with strong warning.
     * 
     * <p>Use this for critical operations that cannot be undone.</p>
     * 
     * @param title the dialog title
     * @param message the warning message to display
     * @return {@code true} if user confirmed, {@code false} otherwise
     */
    protected boolean showDestructiveConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("⚠️ DESTRUCTIVE OPERATION");
        alert.setContentText(message + "\n\nAre you absolutely sure?");
        
        ButtonType yesButton = new ButtonType("Yes, Delete");
        ButtonType noButton = new ButtonType("No, Cancel");
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
    
    /**
     * Displays an informational message dialog.
     * @param title the dialog title
     * @param message the informational message
     */
    protected void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }
    
    /**
     * Gets the current stage for this view.
     * @return the JavaFX stage
     */
    protected Stage getStage() {
        return stage;
    }
    
    /**
     * Gets the root VBox for this view.
     * @return the root VBox container
     */
    protected VBox getRoot() {
        return root;
    }
}