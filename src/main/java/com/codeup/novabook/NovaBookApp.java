package com.codeup.novabook;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeup.novabook.db.ConnectionFactory;
import com.codeup.novabook.exception.UserAlreadyExistsException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.infra.config.AppConfig;
import com.codeup.novabook.repo.IBookRepository;
import com.codeup.novabook.repo.IConfigRepository;
import com.codeup.novabook.repo.ILoanRepository;
import com.codeup.novabook.repo.IMemberRepository;
import com.codeup.novabook.repo.IUserRepository;
import com.codeup.novabook.repo.impl.BookRepositoryImpl;
import com.codeup.novabook.repo.impl.ConfigRepositoryImpl;
import com.codeup.novabook.repo.impl.LoanRepositoryImpl;
import com.codeup.novabook.repo.impl.MemberRepositoryImpl;
import com.codeup.novabook.repo.impl.UserRepositoryImpl;
import com.codeup.novabook.service.IBookService;
import com.codeup.novabook.service.IConfigService;
import com.codeup.novabook.service.ILoanService;
import com.codeup.novabook.service.IMemberService;
import com.codeup.novabook.service.IUserService;
import com.codeup.novabook.service.impl.BookServiceImpl;
import com.codeup.novabook.service.impl.ConfigServiceImpl;
import com.codeup.novabook.service.impl.LoanServiceImpl;
import com.codeup.novabook.service.impl.MemberServiceImpl;
import com.codeup.novabook.service.impl.UserServiceImpl;
import com.codeup.novabook.ui.ServiceContainer;
import com.codeup.novabook.ui.view.LoginView;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * NovaBook Library Management System - Main Entry Point.
 * 
 * <p>This class serves as the main entry point for the NovaBook library management system.
 * It follows SOLID principles and implements dependency injection to configure
 * and initialize all application layers.</p>
 * 
 * <p><strong>Application Architecture:</strong></p>
 * <ul>
 *   <li>Configuration and Database Layer (PostgreSQL with JDBC)</li>
 *   <li>Repository Layer (Data Access Objects)</li>
 *   <li>Service Layer (Business Logic with Transactions)</li>
 *   <li>UI Layer (JavaFX Views with Role-Based Access)</li>
 * </ul>
 * 
 * <p><strong>Initialization Process:</strong></p>
 * <ol>
 *   <li>Load configuration from application.properties</li>
 *   <li>Initialize database connection pool</li>
 *   <li>Create repository instances</li>
 *   <li>Create service instances with dependency injection</li>
 *   <li>Create test users if they don't exist (via service for proper BCrypt hashing)</li>
 *   <li>Launch JavaFX login view</li>
 * </ol>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see javafx.application.Application
 * @see ServiceContainer
 */
public class NovaBookApp extends Application {
    private static final Logger logger = Logger.getLogger(NovaBookApp.class.getName());

    /**
     * Starts the JavaFX application by initializing services and displaying the login view.
     * 
     * <p>This method is called automatically by the JavaFX runtime after the application is launched.</p>
     * 
     * @param primaryStage the primary stage for this application
     * @throws RuntimeException if service initialization fails
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting NovaBook Library Management System...");
        
        try {
            // Initialize dependencies (DI Container)
            ServiceContainer container = initializeServices();
            
            // Start with login view
            LoginView loginView = new LoginView(container);
            loginView.show(primaryStage);
            
            logger.info("NovaBook application started successfully!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start application", e);
            throw new RuntimeException("Application startup failed", e);
        }
    }
    
    /**
     * Initializes and configures all application services using dependency injection.
     * 
     * <p>This method sets up the complete application stack from database connectivity
     * to business services, following the dependency injection pattern for loose coupling.</p>
     * 
     * <p><strong>Initialization Steps:</strong></p>
     * <ol>
     *   <li>Configure database connection with PostgreSQL</li>
     *   <li>Set up repository layer for data access</li>
     *   <li>Initialize service layer for business logic</li>
     *   <li>Create test users via service (proper BCrypt hashing)</li>
     *   <li>Return configured service container</li>
     * </ol>
     * 
     * @return fully configured {@link ServiceContainer} with all dependencies
     * @throws RuntimeException if database connection or user creation fails
     */
    private ServiceContainer initializeServices() {
        logger.info("Initializing application services...");
        
        // Configuration and Database Layer
        AppConfig config = new AppConfig();
        ConnectionFactory connectionFactory = new ConnectionFactory(config);
        
        // Repository Layer (Data Access)
        IUserRepository userRepo = new UserRepositoryImpl(connectionFactory);
        IMemberRepository memberRepo = new MemberRepositoryImpl(connectionFactory);
        IBookRepository bookRepo = new BookRepositoryImpl(connectionFactory);
        ILoanRepository loanRepo = new LoanRepositoryImpl(connectionFactory);
        IConfigRepository configRepo = new ConfigRepositoryImpl(connectionFactory);
        
        // Service Layer (Business Logic)
        IUserService userService = new UserServiceImpl(userRepo);
        IMemberService memberService = new MemberServiceImpl(memberRepo);
        IBookService bookService = new BookServiceImpl(bookRepo);
        IConfigService configService = new ConfigServiceImpl(configRepo);
        ILoanService loanService = new LoanServiceImpl(loanRepo, memberRepo, bookRepo, configService);
        
        // Create test users if they don't exist (via service for proper BCrypt hashing)
        createTestUsersIfNeeded(userService);
        
        logger.info("Services initialized successfully!");
        return new ServiceContainer(userService, memberService, bookService, loanService, configService);
    }
    
    /**
     * Creates test users for development/testing if they don't already exist.
     * 
     * <p>This method uses {@link IUserService#adminRegister()} to ensure passwords
     * are properly hashed with BCrypt. Attempting to insert pre-hashed passwords
     * directly into the database would fail authentication.</p>
     * 
     * <p><strong>Test Users Created:</strong></p>
     * <ul>
     *   <li><strong>Admin:</strong> admin@mail.com / Admin123! (ADMIN role)</li>
     *   <li><strong>Assistant:</strong> asistente@mail.com / Asist123! (ASSISTANT role)</li>
     * </ul>
     * 
     * <p><strong>Note:</strong> These users are only for development/testing.
     * In production, they should be created through the admin interface.</p>
     * 
     * @param userService the user service for creating users with proper password hashing
     */
    @SuppressWarnings("UseSpecificCatch")
    private void createTestUsersIfNeeded(IUserService userService) {
        logger.info("Checking for test users...");
        
        try {
            // Create Admin user if not exists
            if (!userService.userExists("admin@mail.com")) {
                logger.info("Creating test ADMIN user...");
                userService.adminRegister(
                    "Admin System", 
                    "admin@mail.com", 
                    "Admin123!", 
                    "3001234567", 
                    "ADMIN",
                    "FULL"
                );
                logger.info("✅ Admin user created: admin@mail.com / Admin123!");
            } else {
                logger.info("Admin user already exists");
            }
            
            // Create Assistant user if not exists
            if (!userService.userExists("asistente@mail.com")) {
                logger.info("Creating test ASSISTANT user...");
                userService.adminRegister(
                    "María González", 
                    "asistente@mail.com", 
                    "Asist123!", 
                    "3019876543", 
                    "ASSISTANT",
                    "BASIC"
                );
                logger.info("✅ Assistant user created: asistente@mail.com / Asist123!");
            } else {
                logger.info("Assistant user already exists");
            }
            
        } catch (ValidationException | UserAlreadyExistsException e) {
            logger.log(Level.WARNING, "Error creating test users: {0}", e.getMessage());
            // Don't throw - application can continue without test users
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error creating test users", e);
            throw new RuntimeException("Failed to create test users", e);
        }
    }
    
    /**
     * Main method to launch the NovaBook application.
     * 
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}