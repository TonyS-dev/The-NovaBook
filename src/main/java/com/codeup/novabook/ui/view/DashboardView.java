package com.codeup.novabook.ui.view;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.codeup.novabook.domain.Book;
import com.codeup.novabook.domain.Loan;
import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.User;
import com.codeup.novabook.exception.BookNotFoundException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.service.IConfigService;
import com.codeup.novabook.ui.ServiceContainer;
import com.codeup.novabook.ui.helper.CsvHelper;
import com.codeup.novabook.ui.view.dialog.BookDialog;
import com.codeup.novabook.ui.view.dialog.ExtendLoanDialog;
import com.codeup.novabook.ui.view.dialog.LoanDialog;
import com.codeup.novabook.ui.view.dialog.MemberDialog;
import com.codeup.novabook.ui.view.dialog.ReturnLoanDialog;
import com.codeup.novabook.ui.view.dialog.UserDialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main dashboard view with role-based tab control prepared for future permission-based migration.
 * 
 * Tab visibility based on user role:
 * - ADMIN: 6 tabs (Books, Members, Loans, Users, Reports, Config)
 * - ASSISTANT: 3 tabs (Books, Members, Loans)
 * 
 * Access control methods are encapsulated for easy migration to permission-based system.
 */
public class DashboardView extends BaseView {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private TabPane tabPane;
    private TableView<Book> booksTable;
    private TextField searchBookField;
    private CheckBox showInactiveBooksCheckbox;
    private TableView<Member> membersTable;
    private TextField searchMemberField;
    private TableView<Loan> loansTable;
    private ComboBox<String> loanFilterCombo;
    private TableView<User> usersTable;
    
    public DashboardView(ServiceContainer services) {
        super(services);
    }
    
    @Override
    protected String getTitle() {
        return "NovaBook Library System - Dashboard";
    }
    
    @Override
    protected void buildContent() {
        HBox header = createWelcomeHeader();
        root.getChildren().add(header);
        
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        buildTabs();
        
        root.getChildren().add(tabPane);
    }
    
    private HBox createWelcomeHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 5;");
        
        Label welcomeLabel = new Label("Welcome, " + services.getCurrentUser().getName());
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        welcomeLabel.setStyle("-fx-text-fill: white;");
        
        Label roleLabel = new Label(services.getRoleName());
        roleLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> handleLogout());
        
        header.getChildren().addAll(welcomeLabel, roleLabel, spacer, logoutBtn);
        return header;
    }
    
    /**
     * Builds tabs based on user role with encapsulated access control.
     */
    private void buildTabs() {
        tabPane.getTabs().add(createBooksTab());
        
        if (canAccessStaffFeatures()) {
            tabPane.getTabs().add(createMembersTab());
            tabPane.getTabs().add(createLoansTab());
        }
        
        if (canAccessAdminFeatures()) {
            tabPane.getTabs().add(createUsersTab());
            tabPane.getTabs().add(createReportsTab());
            tabPane.getTabs().add(createConfigTab());
        }
    }
    
    /**
     * Encapsulated staff access check for future permission-based migration.
     */
    private boolean canAccessStaffFeatures() {
        return services.isAdmin() || services.isAssistant();
    }
    
    /**
     * Encapsulated admin access check for future permission-based migration.
     */
    private boolean canAccessAdminFeatures() {
        return services.isAdmin();
    }
    
    // ============== TAB CREATION METHODS ==============
    
    private Tab createBooksTab() {
        Tab tab = new Tab("📚 Books");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // Action buttons (no title header)
        HBox actions = new HBox(10);
        Button btnAdd = new Button("➕ Add Book");
        btnAdd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAdd.setOnAction(e -> handleAddBook());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadBooks());
        
        actions.getChildren().add(btnAdd);
        
        // Add import/export buttons only for ADMIN
        if (canAccessAdminFeatures()) {
            Button btnImport = new Button("📥 Import CSV");
            btnImport.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
            btnImport.setOnAction(e -> handleImportBooks());
            
            Button btnExport = new Button("📤 Export CSV");
            btnExport.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
            btnExport.setOnAction(e -> handleExportBooks());
            
            actions.getChildren().addAll(btnImport, btnExport);
        }
        
        actions.getChildren().add(refreshBtn);
        
        // Search filter (simplified inline)
        HBox filters = new HBox(10);
        searchBookField = new TextField();
        searchBookField.setPromptText("🔍 Search books...");
        searchBookField.setPrefWidth(300);
        searchBookField.textProperty().addListener((obs, old, val) -> filterBooks());
        
        // Add checkbox to show/hide inactive books
        showInactiveBooksCheckbox = new CheckBox("Show Inactive Books");
        showInactiveBooksCheckbox.setSelected(false); // Default: hide inactive books
        showInactiveBooksCheckbox.setOnAction(e -> loadBooks());
        
        filters.getChildren().addAll(searchBookField, showInactiveBooksCheckbox);
        
        // Table
        TableView<Book> table = createBooksTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        
        content.getChildren().addAll(actions, filters, table);
        tab.setContent(content);
        
        // Store reference for data loading
        booksTable = table;
        
        // Auto-load books when tab is created
        loadBooks();
        
        return tab;
    }
    
    private Tab createMembersTab() {
        Tab tab = new Tab("👥 Members");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        searchMemberField = new TextField();
        searchMemberField.setPromptText("🔍 Search by name, email, or document...");
        searchMemberField.setPrefWidth(300);
        searchMemberField.textProperty().addListener((obs, old, val) -> filterMembers());
        
        Button addMemberBtn = new Button("➕ Add Member");
        addMemberBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        addMemberBtn.setOnAction(e -> handleAddMember());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadMembers());
        
        if (canAccessAdminFeatures()) {
            Button importMembersBtn = new Button("📥 Import CSV");
            importMembersBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
            importMembersBtn.setOnAction(e -> handleImportMembers());
            
            Button exportMembersBtn = new Button("📤 Export CSV");
            exportMembersBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
            exportMembersBtn.setOnAction(e -> handleExportMembers());
            
            controls.getChildren().addAll(searchMemberField, addMemberBtn, importMembersBtn, exportMembersBtn, refreshBtn);
        } else {
            controls.getChildren().addAll(searchMemberField, addMemberBtn, refreshBtn);
        }
        
        membersTable = createMembersTable();
        VBox.setVgrow(membersTable, Priority.ALWAYS);
        
        content.getChildren().addAll(controls, membersTable);
        tab.setContent(content);
        loadMembers();
        
        return tab;
    }
    
    private Tab createLoansTab() {
        Tab tab = new Tab("📖 Loans");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        loanFilterCombo = new ComboBox<>();
        loanFilterCombo.setItems(FXCollections.observableArrayList("All Loans", "Active Only", "Overdue Only", "Returned Only"));
        loanFilterCombo.setValue("Active Only");
        loanFilterCombo.setOnAction(e -> filterLoans());
        
        Button newLoanBtn = new Button("➕ New Loan");
        newLoanBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        newLoanBtn.setOnAction(e -> handleNewLoan());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadLoans());
        
        if (canAccessAdminFeatures()) {
            Button importLoansBtn = new Button("📥 Import Loans");
            importLoansBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
            importLoansBtn.setOnAction(e -> handleImportLoans());
            
            Button exportLoansBtn = new Button("📤 Export All");
            exportLoansBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
            exportLoansBtn.setOnAction(e -> handleExportLoans());
            
            Button exportOverdueBtn = new Button("📤 Export Overdue");
            exportOverdueBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
            exportOverdueBtn.setOnAction(e -> handleExportOverdue());
            
            controls.getChildren().addAll(loanFilterCombo, newLoanBtn, importLoansBtn, exportLoansBtn, exportOverdueBtn, refreshBtn);
        } else {
            controls.getChildren().addAll(loanFilterCombo, newLoanBtn, refreshBtn);
        }
        
        loansTable = createLoansTable();
        VBox.setVgrow(loansTable, Priority.ALWAYS);
        
        content.getChildren().addAll(controls, loansTable);
        tab.setContent(content);
        loadLoans();
        
        return tab;
    }
    
    private Tab createUsersTab() {
        Tab tab = new Tab("👤 Users");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        
        Button addUserBtn = new Button("➕ Add User");
        addUserBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        addUserBtn.setOnAction(e -> handleAddUser());
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadUsers());
        
        controls.getChildren().addAll(addUserBtn, refreshBtn);
        
        usersTable = createUsersTable();
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        
        content.getChildren().addAll(controls, usersTable);
        tab.setContent(content);
        loadUsers();
        
        return tab;
    }
    
    private Tab createReportsTab() {
        Tab tab = new Tab("📊 Reports");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        
        Label titleLabel = new Label("📊 System Reports");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        VBox reportButtons = new VBox(10);
        reportButtons.setAlignment(Pos.CENTER);
        reportButtons.setPrefWidth(400);
        
        Button overdueReportBtn = createReportButton("📅 Overdue Loans Report", "Generate report of all overdue loans");
        overdueReportBtn.setOnAction(e -> generateOverdueReport());
        
        Button popularBooksBtn = createReportButton("⭐ Popular Books Report", "Books with most loans");
        popularBooksBtn.setOnAction(e -> generatePopularBooksReport());
        
        Button memberActivityBtn = createReportButton("👥 Member Activity Report", "Active members statistics");
        memberActivityBtn.setOnAction(e -> generateMemberActivityReport());
        
        Button inventoryBtn = createReportButton("📚 Inventory Report", "Book stock and availability");
        inventoryBtn.setOnAction(e -> generateInventoryReport());
        
        reportButtons.getChildren().addAll(overdueReportBtn, popularBooksBtn, memberActivityBtn, inventoryBtn);
        
        content.getChildren().addAll(titleLabel, reportButtons);
        tab.setContent(content);
        
        return tab;
    }
    
    private Tab createConfigTab() {
        Tab tab = new Tab("⚙️ Config");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("⚙️ System Configuration");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        VBox configOptions = new VBox(15);
        configOptions.setPrefWidth(500);
        
        // Load current configuration from database
        IConfigService configService = services.getConfigService();
        
        HBox loanDurationBox = new HBox(10);
        loanDurationBox.setAlignment(Pos.CENTER_LEFT);
        Label loanDurationLabel = new Label("Default Loan Duration (days):");
        loanDurationLabel.setPrefWidth(250);
        TextField loanDurationField = new TextField(String.valueOf(configService.getDefaultLoanDays()));
        loanDurationField.setPrefWidth(100);
        loanDurationBox.getChildren().addAll(loanDurationLabel, loanDurationField);
        
        HBox dailyFineBox = new HBox(10);
        dailyFineBox.setAlignment(Pos.CENTER_LEFT);
        Label dailyFineLabel = new Label("Daily Fine Amount:");
        dailyFineLabel.setPrefWidth(250);
        TextField dailyFineField = new TextField(configService.getDailyFineAmount().toString());
        dailyFineField.setPrefWidth(100);
        dailyFineBox.getChildren().addAll(dailyFineLabel, dailyFineField);
        
        HBox maxLoansBox = new HBox(10);
        maxLoansBox.setAlignment(Pos.CENTER_LEFT);
        Label maxLoansLabel = new Label("Max Active Loans per Member:");
        maxLoansLabel.setPrefWidth(250);
        TextField maxLoansField = new TextField(String.valueOf(configService.getMaxActiveLoans()));
        maxLoansField.setPrefWidth(100);
        maxLoansBox.getChildren().addAll(maxLoansLabel, maxLoansField);
        
        Button saveConfigBtn = new Button("�� Save Configuration");
        saveConfigBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        saveConfigBtn.setOnAction(e -> saveConfiguration(loanDurationField.getText(), dailyFineField.getText(), maxLoansField.getText()));
        
        configOptions.getChildren().addAll(loanDurationBox, dailyFineBox, maxLoansBox, saveConfigBtn);
        
        content.getChildren().addAll(titleLabel, configOptions);
        tab.setContent(content);
        
        return tab;
    }
    
    // ============== TABLE CREATION METHODS ==============
    
    @SuppressWarnings("unchecked")
    private TableView<Book> createBooksTable() {
        TableView<Book> table = new TableView<>();
        
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(120);
        
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);
        
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(200);
        
        TableColumn<Book, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        
        TableColumn<Book, String> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(data -> {
            Book book = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(book.isActive() ? "Yes" : "No");
        });
        activeCol.setPrefWidth(70);
        
        TableColumn<Book, Integer> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        availableCol.setPrefWidth(80);
        
        TableColumn<Book, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        totalCol.setPrefWidth(80);
        
        TableColumn<Book, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Edit");
            private final Button deleteBtn = new Button("🗑️ Delete");
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEditBook(getTableView().getItems().get(getIndex())));
                
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> handleDeleteBook(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn));
            }
        });
        
        table.getColumns().addAll(isbnCol, titleCol, authorCol, categoryCol, activeCol, availableCol, totalCol, actionsCol);
        return table;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<Member> createMembersTable() {
        TableView<Member> table = new TableView<>();
        
        TableColumn<Member, String> documentCol = new TableColumn<>("Document");
        documentCol.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        documentCol.setPrefWidth(120);
        
        TableColumn<Member, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Member, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<Member, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130);
        
        TableColumn<Member, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);
        
        TableColumn<Member, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Edit");
            private final Button toggleBtn = new Button("🔄 Toggle");
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEditMember(getTableView().getItems().get(getIndex())));
                
                toggleBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                toggleBtn.setOnAction(e -> handleToggleMemberStatus(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, toggleBtn));
            }
        });
        
        table.getColumns().addAll(documentCol, nameCol, emailCol, phoneCol, statusCol, actionsCol);
        return table;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<Loan> createLoansTable() {
        TableView<Loan> table = new TableView<>();
        
        TableColumn<Loan, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<Loan, String> memberCol = new TableColumn<>("Member");
        memberCol.setCellValueFactory(data -> {
            try {
                Member member = services.getMemberService().getMemberById(data.getValue().getMemberId());
                return new SimpleStringProperty(member.getFirstName() + " " + member.getLastName());
            } catch (MemberNotFoundException e) {
                return new SimpleStringProperty("Unknown");
            }
        });
        memberCol.setPrefWidth(150);
        
        TableColumn<Loan, String> bookCol = new TableColumn<>("Book");
        bookCol.setCellValueFactory(data -> {
            try {
                Book book = services.getBookService().getBookById(data.getValue().getBookId());
                return new SimpleStringProperty(book.getTitle());
            } catch (BookNotFoundException e) {
                return new SimpleStringProperty("Unknown");
            }
        });
        bookCol.setPrefWidth(200);
        
        TableColumn<Loan, String> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLoanDate().format(DATE_FORMATTER)));
        loanDateCol.setPrefWidth(100);
        
        TableColumn<Loan, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpectedReturnDate().format(DATE_FORMATTER)));
        dueDateCol.setPrefWidth(100);
        
        TableColumn<Loan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);
        
        TableColumn<Loan, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button returnBtn = new Button("✅ Return");
            private final Button extendBtn = new Button("⏰ Extend");
            
            {
                returnBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                returnBtn.setOnAction(e -> handleReturnLoan(getTableView().getItems().get(getIndex())));
                
                extendBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                extendBtn.setOnAction(e -> handleExtendLoan(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Loan loan = getTableView().getItems().get(getIndex());
                    String status = loan.getStatus().name();
                    
                    switch (status) {
                        case "RETURNED" -> // No buttons for returned loans
                            setGraphic(null);
                        case "ACTIVE" -> // Both buttons for active loans
                            setGraphic(new HBox(5, returnBtn, extendBtn));
                        default -> // Only return button for overdue loans
                            setGraphic(new HBox(5, returnBtn));
                    }
                }
            }
        });
        
        table.getColumns().addAll(idCol, memberCol, bookCol, loanDateCol, dueDateCol, statusCol, actionsCol);
        return table;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<User> createUsersTable() {
        TableView<User> table = new TableView<>();
        
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);
        
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        roleCol.setPrefWidth(100);
        
        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        statusCol.setPrefWidth(100);
        
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Edit");
            private final Button toggleBtn = new Button("🔄 Toggle");
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEditUser(getTableView().getItems().get(getIndex())));
                
                toggleBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                toggleBtn.setOnAction(e -> handleToggleUserStatus(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    if (user.getId().equals(services.getCurrentUser().getId())) {
                        setGraphic(null);
                    } else {
                        setGraphic(new HBox(5, editBtn, toggleBtn));
                    }
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, statusCol, actionsCol);
        return table;
    }
    
    // ============== DATA LOADING METHODS ==============
    
    private void loadBooks() {
        List<Book> books = services.getBookService().getAllBooks();
        
        // Filter inactive books by default unless checkbox is selected
        if (!showInactiveBooksCheckbox.isSelected()) {
            books = books.stream()
                    .filter(Book::isActive)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        booksTable.setItems(FXCollections.observableArrayList(books));
    }
    
    private void loadMembers() {
        List<Member> members = services.getMemberService().getAllMembers();
        membersTable.setItems(FXCollections.observableArrayList(members));
    }
    
    private void loadLoans() {
        List<Loan> loans = services.getLoanService().getAllLoans();
        loansTable.setItems(FXCollections.observableArrayList(loans));
        filterLoans();
    }
    
    private void loadUsers() {
        List<User> users = services.getUserService().list();
        usersTable.setItems(FXCollections.observableArrayList(users));
    }
    
    // ============== FILTER METHODS ==============
    
    private void filterBooks() {
        String search = searchBookField.getText().toLowerCase().trim();
        if (search.isEmpty()) {
            loadBooks();
            return;
        }
        
        List<Book> filtered = services.getBookService().getAllBooks().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(search) ||
                               book.getAuthor().toLowerCase().contains(search) ||
                               book.getIsbn().toLowerCase().contains(search))
                .toList();
        
        // Also apply active/inactive filter
        if (!showInactiveBooksCheckbox.isSelected()) {
            filtered = filtered.stream()
                    .filter(Book::isActive)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        booksTable.setItems(FXCollections.observableArrayList(filtered));
    }
    
    private void filterMembers() {
        String search = searchMemberField.getText().toLowerCase().trim();
        if (search.isEmpty()) {
            loadMembers();
            return;
        }
        
        List<Member> filtered = services.getMemberService().getAllMembers().stream()
                .filter(member -> (member.getFirstName() + " " + member.getLastName()).toLowerCase().contains(search) ||
                                 member.getEmail().toLowerCase().contains(search) ||
                                 member.getDocumentId().toLowerCase().contains(search))
                .toList();
        
        membersTable.setItems(FXCollections.observableArrayList(filtered));
    }
    
    private void filterLoans() {
        String filter = loanFilterCombo.getValue();
        List<Loan> allLoans = services.getLoanService().getAllLoans();
        
        List<Loan> filtered = switch (filter) {
            case "Active Only" -> allLoans.stream().filter(loan -> loan.getStatus().name().equals("ACTIVE")).toList();
            case "Overdue Only" -> services.getLoanService().getOverdueLoans();
            case "Returned Only" -> allLoans.stream().filter(loan -> loan.getStatus().name().equals("RETURNED")).toList();
            default -> allLoans;
        };
        
        loansTable.setItems(FXCollections.observableArrayList(filtered));
    }
    
    // ============== ACTION HANDLERS ==============
    
    private void handleAddBook() {
        BookDialog dialog = new BookDialog(services.getBookService());
        dialog.showAndWaitResult().ifPresent(book -> {
            showSuccess("Book added successfully: " + book.getTitle());
            loadBooks();
        });
    }
    
    private void handleEditBook(Book book) {
        BookDialog dialog = new BookDialog(services.getBookService(), book);
        dialog.showAndWaitResult().ifPresent(updatedBook -> {
            showSuccess("Book updated successfully: " + updatedBook.getTitle());
            loadBooks();
        });
    }
    
    private void handleDeleteBook(Book book) {
        // Check if already inactive
        if (!book.getActive()) {
            showWarning("This book is already deactivated.");
            return;
        }
        
        if (showDestructiveConfirmation("Delete Book", "Are you sure you want to delete '" + book.getTitle() + "'?")) {
            try {
                services.getBookService().deactivateBook(book.getId());
                showSuccess("Book deactivated successfully");
                loadBooks();
            } catch (BookNotFoundException e) {
                showError("Error deactivating book: " + e.getMessage());
            }
        }
    }
    
    private void handleImportBooks() {
        try {
            CsvHelper.ImportResult result = CsvHelper.importBooks(services.getBookService(), getStage());
            if (result == null) {
                return; // User cancelled
            }
            
            // Show result summary
            StringBuilder message = new StringBuilder(result.getSummary());
            if (result.getErrorCount() > 0) {
                message.append("\n\nErrors:\n");
                int shown = Math.min(5, result.getErrors().size());
                for (int i = 0; i < shown; i++) {
                    message.append("• ").append(result.getErrors().get(i)).append("\n");
                }
                if (result.getErrors().size() > 5) {
                    message.append("... and ").append(result.getErrors().size() - 5).append(" more errors");
                }
            }
            
            if (result.getSuccessCount() > 0) {
                showSuccess(message.toString());
                loadBooks();
            } else {
                showError(message.toString());
            }
        } catch (Exception e) {
            showError("Import failed: " + e.getMessage());
        }
    }
    
    private void handleExportBooks() {
        try {
            List<Book> books = services.getBookService().getAllBooks();
            java.io.File file = CsvHelper.exportBooksCatalog(books, getStage());
            if (file != null) {
                showSuccess("Books exported successfully to:\n" + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private void handleAddMember() {
        MemberDialog dialog = new MemberDialog(services.getMemberService());
        dialog.showAndWaitResult().ifPresent(member -> {
            showSuccess("Member registered successfully: " + member.getFullName());
            loadMembers();
        });
    }
    
    private void handleEditMember(Member member) {
        MemberDialog dialog = new MemberDialog(services.getMemberService(), member);
        dialog.showAndWaitResult().ifPresent(updatedMember -> {
            showSuccess("Member updated successfully: " + updatedMember.getFullName());
            loadMembers();
        });
    }
    
    private void handleToggleMemberStatus(Member member) {
        String action = member.isActive() ? "deactivate" : "activate";
        if (showConfirmation("Toggle Status", "Do you want to " + action + " member '" + member.getFirstName() + " " + member.getLastName() + "'?")) {
            try {
                if (member.isActive()) {
                    services.getMemberService().deactivateMember(member.getId());
                    showSuccess("Member deactivated successfully");
                } else {
                    services.getMemberService().activateMember(member.getId());
                    showSuccess("Member activated successfully");
                }
                loadMembers();
            } catch (MemberNotFoundException e) {
                showError("Error updating status: " + e.getMessage());
            }
        }
    }
    
    private void handleNewLoan() {
        LoanDialog dialog = new LoanDialog(services.getLoanService(), 
                                           services.getMemberService(), 
                                           services.getBookService(),
                                           services.getConfigService());
        dialog.showAndWaitResult().ifPresent(loan -> {
            showSuccess("Loan created successfully (ID: " + loan.getId() + ")");
            loadLoans();
        });
    }
    
    private void handleReturnLoan(Loan loan) {
        ReturnLoanDialog dialog = new ReturnLoanDialog(services.getLoanService(), 
                                                        services.getConfigService(), loan);
        dialog.showAndWaitResult().ifPresent(returnedLoan -> {
            showSuccess("Loan returned successfully");
            loadLoans();
        });
    }
    
    private void handleExtendLoan(Loan loan) {
        ExtendLoanDialog dialog = new ExtendLoanDialog(services.getLoanService(), loan);
        dialog.showAndWaitResult().ifPresent(extendedLoan -> {
            showSuccess("Loan extended successfully. New due date: " + 
                       extendedLoan.getExpectedReturnDate());
            loadLoans();
        });
    }
    
    private void handleExportOverdue() {
        try {
            List<Loan> overdueLoans = services.getLoanService().getOverdueLoans();
            if (overdueLoans.isEmpty()) {
                showInfo("Export Overdue Loans", "No overdue loans found");
                return;
            }
            
            java.io.File file = CsvHelper.exportOverdueLoans(overdueLoans, 
                    services.getMemberService(), services.getBookService(), getStage());
            if (file != null) {
                showSuccess("Overdue loans exported successfully:\n" + file.getAbsolutePath() + 
                          "\n\nTotal: " + overdueLoans.size() + " overdue loans");
            }
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private void handleExportLoans() {
        try {
            List<Loan> allLoans = services.getLoanService().getAllLoans();
            if (allLoans.isEmpty()) {
                showInfo("Export Loans", "No loans found to export");
                return;
            }
            
            java.io.File file = CsvHelper.exportLoans(allLoans, 
                    services.getMemberService(), services.getBookService(), getStage());
            if (file != null) {
                showSuccess("All loans exported successfully:\n" + file.getAbsolutePath() + 
                          "\n\nTotal: " + allLoans.size() + " loans");
            }
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private void handleImportLoans() {
        try {
            int imported = CsvHelper.importLoans(services.getLoanService(), 
                    services.getMemberService(), services.getBookService(), getStage());
            if (imported > 0) {
                showSuccess("Loans imported successfully!\nTotal imported: " + imported);
                loadLoans();
            }
        } catch (Exception e) {
            showError("Import failed: " + e.getMessage());
        }
    }
    
    private void handleExportMembers() {
        try {
            List<Member> allMembers = services.getMemberService().getAllMembers();
            if (allMembers.isEmpty()) {
                showInfo("Export Members", "No members found to export");
                return;
            }
            
            java.io.File file = CsvHelper.exportMembers(allMembers, getStage());
            if (file != null) {
                showSuccess("Members exported successfully:\n" + file.getAbsolutePath() + 
                          "\n\nTotal: " + allMembers.size() + " members");
            }
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private void handleImportMembers() {
        try {
            int imported = CsvHelper.importMembers(services.getMemberService(), getStage());
            if (imported > 0) {
                showSuccess("Members imported successfully!\nTotal imported: " + imported);
                loadMembers();
            }
        } catch (Exception e) {
            showError("Import failed: " + e.getMessage());
        }
    }
    
    private void handleAddUser() {
        UserDialog dialog = new UserDialog(services.getUserService());
        if (dialog.showAndWaitConfirmation()) {
            showSuccess("User created successfully");
            loadUsers();
        }
    }
    
    private void handleEditUser(User user) {
        // Create custom dialog
        javafx.scene.control.Dialog<User> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getEmail());
        
        // Create form
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(user.getName());
        TextField emailField = new TextField(user.getEmail());
        TextField phoneField = new TextField(user.getPhone());
        ComboBox<com.codeup.novabook.domain.UserRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(com.codeup.novabook.domain.UserRole.values());
        roleCombo.setValue(user.getRole());
        
        ComboBox<com.codeup.novabook.domain.UserStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(com.codeup.novabook.domain.UserStatus.values());
        statusCombo.setValue(user.getStatus());
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK, 
            javafx.scene.control.ButtonType.CANCEL
        );
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                User updatedUser = new User();
                updatedUser.setName(nameField.getText());
                updatedUser.setEmail(emailField.getText());
                updatedUser.setPhone(phoneField.getText());
                updatedUser.setRole(roleCombo.getValue());
                updatedUser.setStatus(statusCombo.getValue());
                return updatedUser;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(updatedUser -> {
            try {
                // Update using the old email to find the user
                services.getUserService().updateUserByEmail(updatedUser, user.getEmail());
                showSuccess("User updated successfully");
                loadUsers();
            } catch (Exception e) {
                showError("Error updating user: " + e.getMessage());
            }
        });
    }
    
    private void handleToggleUserStatus(User user) {
        String action = user.isActive() ? "deactivate" : "activate";
        if (showConfirmation("Toggle Status", "Do you want to " + action + " user '" + user.getName() + "'?")) {
            try {
                User updatedUser = new User();
                updatedUser.setName(user.getName());
                updatedUser.setPhone(user.getPhone());
                updatedUser.setRole(user.getRole());
                updatedUser.setStatus(user.isActive() ? 
                    com.codeup.novabook.domain.UserStatus.INACTIVE : 
                    com.codeup.novabook.domain.UserStatus.ACTIVE);
                
                services.getUserService().updateUserByEmail(updatedUser, user.getEmail());
                showSuccess("User status updated successfully");
                loadUsers();
            } catch (Exception e) {
                showError("Error updating status: " + e.getMessage());
            }
        }
    }
    
    private void handleLogout() {
        if (showConfirmation("Logout", "Are you sure you want to logout?")) {
            services.setCurrentUser(null);
            navigateToLogin();
        }
    }
    
    // ============== REPORT METHODS ==============
    
    private Button createReportButton(String title, String description) {
        Button btn = new Button(title);
        btn.setPrefWidth(350);
        btn.setPrefHeight(60);
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        btn.setTooltip(new Tooltip(description));
        return btn;
    }
    
    private void generateOverdueReport() {
        List<Loan> overdueLoans = services.getLoanService().getOverdueLoans();
        
        if (overdueLoans.isEmpty()) {
            showInfo("Overdue Report", "No overdue loans found!");
            return;
        }
        
        try {
            CsvHelper.exportLoans(overdueLoans, services.getMemberService(), 
                services.getBookService(), stage.getScene().getWindow());
            showSuccess("""
                        Overdue loans report exported successfully!
                        Found """ + overdueLoans.size() + " overdue loans.");
        } catch (IOException e) {
            showError("Error exporting report: " + e.getMessage());
        }
    }
    
    private void generatePopularBooksReport() {
        try {
            List<Loan> allLoans = services.getLoanService().getAllLoans();
            
            // Count loans per book
            java.util.Map<Integer, Long> loanCountsByBook = allLoans.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Loan::getBookId, 
                    java.util.stream.Collectors.counting()
                ));
            
            // Get books sorted by loan count
            List<Book> popularBooks = services.getBookService().getAllBooks().stream()
                .sorted((b1, b2) -> {
                    long count1 = loanCountsByBook.getOrDefault(b1.getId(), 0L);
                    long count2 = loanCountsByBook.getOrDefault(b2.getId(), 0L);
                    return Long.compare(count2, count1); // Descending order
                })
                .limit(50) // Top 50 most popular
                .collect(java.util.stream.Collectors.toList());
            
            if (popularBooks.isEmpty()) {
                showInfo("Popular Books Report", "No books found in the system!");
                return;
            }
            
            // Export the popular books
            CsvHelper.exportBooksCatalog(popularBooks, stage.getScene().getWindow());
            showSuccess("""
                        Popular books report exported successfully!
                        Exported top """ + popularBooks.size() + " most loaned books.");
        } catch (IOException e) {
            showError("Error generating report: " + e.getMessage());
        }
    }
    
    private void generateMemberActivityReport() {
        try {
            List<Member> members = services.getMemberService().getAllMembers();
            List<Loan> allLoans = services.getLoanService().getAllLoans();
            
            // Count loans per member
            java.util.Map<Integer, Long> loanCountsByMember = allLoans.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Loan::getMemberId, 
                    java.util.stream.Collectors.counting()
                ));
            
            // Sort members by activity (loan count)
            List<Member> activeMembers = members.stream()
                .sorted((m1, m2) -> {
                    long count1 = loanCountsByMember.getOrDefault(m1.getId(), 0L);
                    long count2 = loanCountsByMember.getOrDefault(m2.getId(), 0L);
                    return Long.compare(count2, count1); // Descending order
                })
                .collect(java.util.stream.Collectors.toList());
            
            if (activeMembers.isEmpty()) {
                showInfo("Member Activity Report", "No members found in the system!");
                return;
            }
            
            // Export members
            CsvHelper.exportMembers(activeMembers, stage.getScene().getWindow());
            showSuccess("""
                        Member activity report exported successfully!
                        Exported """ + activeMembers.size() + " members sorted by activity.");
        } catch (IOException e) {
            showError("Error generating report: " + e.getMessage());
        }
    }
    
    private void generateInventoryReport() {
        List<Book> books = services.getBookService().getAllBooks();
        int totalBooks = books.stream().mapToInt(Book::getTotalCopies).sum();
        int availableBooks = books.stream().mapToInt(Book::getAvailableCopies).sum();
        
        showInfo("Inventory Report", 
                String.format("Total books: %d titles\nTotal copies: %d\nAvailable: %d\nOn loan: %d",
                        books.size(), totalBooks, availableBooks, totalBooks - availableBooks));
    }
    
    private void saveConfiguration(String loanDuration, String dailyFine, String maxLoans) {
        try {
            IConfigService configService = services.getConfigService();
            
            // Validate and save each configuration
            if (!configService.validateConfigValue("DEFAULT_LOAN_DAYS", loanDuration)) {
                showError("Invalid loan duration. Must be a positive integer.");
                return;
            }
            
            if (!configService.validateConfigValue("DAILY_FINE_AMOUNT", dailyFine)) {
                showError("Invalid daily fine amount. Must be a positive number.");
                return;
            }
            
            if (!configService.validateConfigValue("MAX_ACTIVE_LOANS", maxLoans)) {
                showError("Invalid max loans. Must be a positive integer.");
                return;
            }
            
            // Save all configurations
            configService.updateConfiguration("DEFAULT_LOAN_DAYS", loanDuration);
            configService.updateConfiguration("DAILY_FINE_AMOUNT", dailyFine);
            configService.updateConfiguration("MAX_ACTIVE_LOANS", maxLoans);
            
            // Reload cache to ensure fresh data
            configService.reloadCache();
            
            showInfo("Configuration Saved", 
                """
                Configuration updated successfully!
                
                Loan Duration: """ + loanDuration + " days\n" +
                "Daily Fine: $" + dailyFine + "\n" +
                "Max Loans: " + maxLoans);
                
        } catch (Exception e) {
            showError("Failed to save configuration: " + e.getMessage());
        }
    }
    
    private void navigateToLogin() {
        try {
            LoginView loginView = new LoginView(services);
            
            // Get the current stage
            Stage currentStage = getStage();
            if (currentStage != null) {
                // Close the current stage
                currentStage.close();
                
                // Create and show new login stage
                Stage loginStage = new Stage();
                loginView.show(loginStage);
            } else {
                // Fallback: try to get stage from scene
                Stage loginStage = (Stage) getRoot().getScene().getWindow();
                if (loginStage != null) {
                    loginStage.close();
                    Stage newLoginStage = new Stage();
                    loginView.show(newLoginStage);
                }
            }
        } catch (Exception e) {
            showError("Error navigating to login: " + e.getMessage());
        }
    }
}
