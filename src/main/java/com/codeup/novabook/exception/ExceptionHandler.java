package com.codeup.novabook.exception;

import javafx.scene.control.Alert;

/**
 * Utility class for handling NovaBook exceptions in the UI layer.
 * <p>
 * This class provides standardized methods for displaying exception messages
 * to users in a consistent and user-friendly manner. It translates technical
 * error codes and messages into appropriate UI alerts based on severity levels.
 * </p>
 * 
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Severity-based alert type mapping</li>
 *   <li>Consistent error message formatting</li>
 *   <li>User-friendly error displays</li>
 *   <li>Centralized exception handling for UI</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * try {
 *     userService.register(name, email, password, phone);
 *     // Your code here
 * } catch (NovaBookException e) {
 *     ExceptionHandler.displayError(e, stage);
 * }
 * }</pre>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 2.0
 */
public class ExceptionHandler {

    /**
     * Displays an error dialog based on a NovaBookException.
     * <p>
     * This method creates an appropriate JavaFX Alert dialog based on the
     * exception's severity level and displays the standardized error message.
     * </p>
     * 
     * @param exception the NovaBookException to display
     */
    public static void showErrorDialog(NovaBookException exception) {
        Alert.AlertType alertType = mapSeverityToAlertType(exception.getSeverity());
        
        Alert alert = new Alert(alertType);
        alert.setTitle("NovaBook - " + exception.getSeverity().name());
        alert.setHeaderText(String.format("Error Code: %s", exception.getCode()));
        alert.setContentText(exception.getMessage());
        
        alert.showAndWait();
    }

    /**
     * Displays an error dialog with custom title and message.
     * <p>
     * This method is useful when you want to provide additional context
     * or a more user-friendly title while still showing the technical
     * error information.
     * </p>
     * 
     * @param exception the EcoFleetException to display
     * @param customTitle custom title for the dialog
     * @param additionalMessage additional user-friendly message
     */
    public static void showErrorDialog(NovaBookException exception, String customTitle, String additionalMessage) {
        Alert.AlertType alertType = mapSeverityToAlertType(exception.getSeverity());
        
        Alert alert = new Alert(alertType);
        alert.setTitle(customTitle);
        alert.setHeaderText(String.format("Error Code: %s", exception.getCode()));
        alert.setContentText(additionalMessage + "\n\nDetails: " + exception.getMessage());
        
        alert.showAndWait();
    }

    /**
     * Shows a simple error message without technical details.
     * <p>
     * This method is useful for showing user-friendly error messages
     * without exposing technical error codes to end users.
     * </p>
     * 
     * @param exception the EcoFleetException to display
     */
    public static void showSimpleErrorDialog(NovaBookException exception) {
        Alert.AlertType alertType = mapSeverityToAlertType(exception.getSeverity());
        
        Alert alert = new Alert(alertType);
        alert.setTitle("NovaBook");
        alert.setHeaderText(null);
        alert.setContentText(exception.getMessage());
        
        alert.showAndWait();
    }

    /**
     * Maps exception severity to JavaFX Alert types.
     * 
     * @param severity the severity level to map
     * @return the corresponding JavaFX Alert.AlertType
     */
    private static Alert.AlertType mapSeverityToAlertType(Severity severity) {
        return switch (severity) {
            case INFO -> Alert.AlertType.INFORMATION;
            case WARN -> Alert.AlertType.WARNING;
            case ERROR -> Alert.AlertType.ERROR;
            case FATAL -> Alert.AlertType.ERROR;
        };
    }

    /**
     * Logs the exception for debugging and monitoring purposes.
     * <p>
     * This method can be extended to integrate with logging frameworks
     * or monitoring systems for production environments.
     * </p>
     * 
     * @param exception the NovaBookException to log
     */
    public static void logException(NovaBookException exception) {
        System.err.printf("[%s] %s: %s%n", 
            exception.getSeverity(), 
            exception.getCode(), 
            exception.getMessage());
    }

    /**
     * Handles an exception by both logging it and showing it to the user.
     * <p>
     * This is a convenience method that combines logging and user notification
     * in a single call, suitable for most exception handling scenarios.
     * </p>
     * 
     * @param exception the NovaBookException to handle
     */
    public static void handleException(NovaBookException exception) {
        logException(exception);
        showErrorDialog(exception);
    }
}