package com.codeup.novabook.ui.view.dialog;

import java.util.Optional;

import com.codeup.novabook.domain.Member;
import com.codeup.novabook.exception.MemberAlreadyExistsException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.service.IMemberService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for creating or editing library members.
 * Provides form validation for document ID uniqueness, email format, and required fields.
 */
public class MemberDialog extends Stage {
    
    private final IMemberService memberService;
    private final Member existingMember; // null for new member, non-null for edit
    
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField documentIdField;
    private TextField emailField;
    private TextField phoneField;
    private TextArea addressArea;
    
    private boolean confirmed = false;
    private Member resultMember;
    
    /**
     * Constructor for adding a new member
     */
    public MemberDialog(IMemberService memberService) {
        this(memberService, null);
    }
    
    /**
     * Constructor for editing an existing member
     */
    public MemberDialog(IMemberService memberService, Member member) {
        this.memberService = memberService;
        this.existingMember = member;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle(member == null ? "👤 Register New Member" : "✏️ Edit Member");
        setResizable(false);
        
        buildUI();
    }
    
    private void buildUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        
        // First name field
        Label firstNameLabel = new Label("First Name *");
        firstNameField = new TextField();
        firstNameField.setPromptText("John");
        firstNameField.setPrefWidth(300);
        grid.add(firstNameLabel, 0, 0);
        grid.add(firstNameField, 1, 0);
        
        // Last name field
        Label lastNameLabel = new Label("Last Name *");
        lastNameField = new TextField();
        lastNameField.setPromptText("Doe");
        lastNameField.setPrefWidth(300);
        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameField, 1, 1);
        
        // Document ID field
        Label documentIdLabel = new Label("Document ID *");
        documentIdField = new TextField();
        documentIdField.setPromptText("12345678");
        documentIdField.setPrefWidth(300);
        // Document ID is now editable to allow fixing typos
        grid.add(documentIdLabel, 0, 2);
        grid.add(documentIdField, 1, 2);
        
        // Email field
        Label emailLabel = new Label("Email *");
        emailField = new TextField();
        emailField.setPromptText("john.doe@email.com");
        emailField.setPrefWidth(300);
        grid.add(emailLabel, 0, 3);
        grid.add(emailField, 1, 3);
        
        // Phone field
        Label phoneLabel = new Label("Phone");
        phoneField = new TextField();
        phoneField.setPromptText("3001234567");
        phoneField.setPrefWidth(300);
        grid.add(phoneLabel, 0, 4);
        grid.add(phoneField, 1, 4);
        
        // Address area
        Label addressLabel = new Label("Address");
        addressArea = new TextArea();
        addressArea.setPromptText("Home address...");
        addressArea.setPrefRowCount(3);
        addressArea.setPrefWidth(300);
        addressArea.setWrapText(true);
        grid.add(addressLabel, 0, 5);
        grid.add(addressArea, 1, 5);
        
        // Required fields note
        Label noteLabel = new Label("* Required fields");
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        grid.add(noteLabel, 1, 6);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button saveBtn = new Button(existingMember == null ? "➕ Register" : "💾 Save");
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
        if (existingMember != null) {
            populateFields();
        }
        
        Scene scene = new Scene(grid);
        setScene(scene);
    }
    
    private void populateFields() {
        firstNameField.setText(existingMember.getFirstName());
        lastNameField.setText(existingMember.getLastName());
        documentIdField.setText(existingMember.getDocumentId());
        emailField.setText(existingMember.getEmail());
        phoneField.setText(existingMember.getPhone() != null ? existingMember.getPhone() : "");
        addressArea.setText(existingMember.getAddress() != null ? existingMember.getAddress() : "");
    }
    
    private void handleSave() {
        try {
            // Validate required fields
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String documentId = documentIdField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            
            if (firstName.isEmpty()) {
                showError("First name is required");
                firstNameField.requestFocus();
                return;
            }
            
            if (lastName.isEmpty()) {
                showError("Last name is required");
                lastNameField.requestFocus();
                return;
            }
            
            if (documentId.isEmpty()) {
                showError("Document ID is required");
                documentIdField.requestFocus();
                return;
            }
            
            if (email.isEmpty()) {
                showError("Email is required");
                emailField.requestFocus();
                return;
            }
            
            // Basic email validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showError("Invalid email format");
                emailField.requestFocus();
                return;
            }
            
            // Check document ID uniqueness
            if (existingMember == null) {
                // For new members, check if document ID exists
                try {
                    memberService.getMemberByDocumentId(documentId);
                    showError("A member with this document ID already exists");
                    documentIdField.requestFocus();
                    return;
                } catch (MemberNotFoundException e) {
                    // Document ID doesn't exist, which is good for new members
                }
            } else {
                // For editing, check if document ID changed and if new one exists
                if (!documentId.equals(existingMember.getDocumentId())) {
                    try {
                        Member existingWithDoc = memberService.getMemberByDocumentId(documentId);
                        // If we found a member with this doc ID and it's not the current member
                        if (!existingWithDoc.getId().equals(existingMember.getId())) {
                            showError("A member with this document ID already exists");
                            documentIdField.requestFocus();
                            return;
                        }
                    } catch (MemberNotFoundException e) {
                        // Document ID doesn't exist, which is good
                    }
                }
            }
            
            // Save via service
            if (existingMember == null) {
                // registerMember(firstName, lastName, documentId, email, phone, address)
                resultMember = memberService.registerMember(firstName, lastName, documentId,
                        email, phone.isEmpty() ? null : phone, address.isEmpty() ? null : address);
            } else {
                // For editing, update all fields
                resultMember = memberService.updateMemberProfile(existingMember.getId(),
                        firstName, lastName, phone.isEmpty() ? null : phone, address.isEmpty() ? null : address);
                
                // Update email if changed
                if (!email.equals(existingMember.getEmail())) {
                    resultMember = memberService.updateMemberEmail(existingMember.getId(), email);
                }
                
                // Note: Document ID edits are allowed in UI but currently not persisted
                // to database due to lack of service method. Consider adding updateDocumentId()
                // method to IMemberService if document ID persistence is required.
            }
            
            confirmed = true;
            close();
            
        } catch (ValidationException e) {
            showError("Validation error: " + e.getMessage());
        } catch (MemberAlreadyExistsException | MemberNotFoundException e) {
            showError("Error saving member: " + e.getMessage());
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
     * @return Optional containing the saved member if confirmed, empty otherwise
     */
    public Optional<Member> showAndWaitResult() {
        showAndWait();
        return confirmed ? Optional.ofNullable(resultMember) : Optional.empty();
    }
}
