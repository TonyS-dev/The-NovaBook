package com.codeup.novabook.ui;

import com.codeup.novabook.domain.User;
import com.codeup.novabook.domain.UserRole;
import com.codeup.novabook.service.IUserService;
import com.codeup.novabook.service.IMemberService;
import com.codeup.novabook.service.IBookService;
import com.codeup.novabook.service.ILoanService;

/**
 * Simple Dependency Injection container for managing application services and user session.
 * 
 * <p>This container follows the Dependency Injection pattern and provides centralized
 * access to business services throughout the user interface layer. It also manages
 * the current user session and provides authentication/authorization utilities.</p>
 * 
 * <p><strong>Design Principles:</strong></p>
 * <ul>
 *   <li><strong>Single Responsibility</strong> - Focuses solely on service management</li>
 *   <li><strong>Dependency Injection</strong> - Services are injected via constructor</li>
 *   <li><strong>Session Management</strong> - Maintains current user state</li>
 *   <li><strong>Authorization</strong> - Provides role-based access checking (ADMIN/ASSISTANT/MEMBER)</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // Initialize services
 * IUserService userService = new UserServiceImpl(userRepository);
 * IMemberService memberService = new MemberServiceImpl(memberRepository);
 * IBookService bookService = new BookServiceImpl(bookRepository);
 * ILoanService loanService = new LoanServiceImpl(loanRepository);
 * 
 * // Create container
 * ServiceContainer services = new ServiceContainer(userService, memberService, bookService, loanService);
 * 
 * // Use in UI components
 * LoginView loginView = new LoginView(services);
 * DashboardView dashboardView = new DashboardView(services);
 * 
 * // Session management
 * User authenticatedUser = services.getUserService().authenticate(email, password);
 * services.setCurrentUser(authenticatedUser);
 * 
 * if (services.isAdmin()) {
 *     // Show admin-only features (Users tab, Config tab, Exports)
 * } else if (services.isAssistant()) {
 *     // Show assistant features (read-only books, manage members/loans)
 * } else if (services.isMember()) {
 *     // Show member features (view catalog, view own loans)
 * }
 * }</pre>
 * 
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is not thread-safe. Each UI session should have its own instance
 * of ServiceContainer to avoid concurrent modification issues.</p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 * @see IUserService
 * @see IMemberService
 * @see IBookService
 * @see ILoanService
 * @see User
 * @see UserRole
 */
public class ServiceContainer {
    private final IUserService userService;
    private final IMemberService memberService;
    private final IBookService bookService;
    private final ILoanService loanService;
    private User currentUser;
    
    /**
     * Constructs a new ServiceContainer with the specified services.
     * 
     * <p>All services are required and must be properly initialized before
     * passing to this constructor.</p>
     * 
     * @param userService the user management service
     * @param memberService the member management service
     * @param bookService the book management service
     * @param loanService the loan management service
     * @throws NullPointerException if any service is null
     */
    public ServiceContainer(IUserService userService, IMemberService memberService, 
                          IBookService bookService, ILoanService loanService) {
        this.userService = userService;
        this.memberService = memberService;
        this.bookService = bookService;
        this.loanService = loanService;
    }
    
    /**
     * Gets the user management service.
     * 
     * @return the user service instance
     */
    public IUserService getUserService() { 
        return userService; 
    }
    
    /**
     * Gets the member management service.
     * 
     * @return the member service instance
     */
    public IMemberService getMemberService() { 
        return memberService; 
    }
    
    /**
     * Gets the book management service.
     * 
     * @return the book service instance
     */
    public IBookService getBookService() { 
        return bookService; 
    }
    
    /**
     * Gets the loan management service.
     * 
     * @return the loan service instance
     */
    public ILoanService getLoanService() { 
        return loanService; 
    }
    
    /**
     * Gets the currently authenticated user.
     * 
     * @return the current user, or {@code null} if no user is logged in
     */
    public User getCurrentUser() { return currentUser; }
    
    /**
     * Sets the currently authenticated user for the session.
     * 
     * <p>This method should be called after successful authentication
     * to establish the user session.</p>
     * 
     * @param user the authenticated user, or {@code null} to clear the session
     */
    public void setCurrentUser(User user) { this.currentUser = user; }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return {@code true} if a user is logged in, {@code false} otherwise
     */
    public boolean isLoggedIn() { 
        return currentUser != null; 
    }
    
    /**
     * Checks if the current user has administrator privileges.
     * 
     * <p>Admin users have full access to all system features including:</p>
     * <ul>
     *   <li>User management (create, edit, deactivate users)</li>
     *   <li>Book management (register, edit, activate/deactivate books)</li>
     *   <li>Member management (full CRUD operations)</li>
     *   <li>Loan management (create, return, extend loans)</li>
     *   <li>Reports and exports (CSV exports, statistics)</li>
     *   <li>System configuration</li>
     * </ul>
     * 
     * @return {@code true} if the current user is an ADMIN, {@code false} otherwise
     */
    public boolean isAdmin() { 
        return currentUser != null 
            && currentUser.getRole() != null 
            && UserRole.ADMIN.equals(currentUser.getRole()); 
    }
    
    /**
     * Checks if the current user is an assistant (library staff).
     * 
     * <p>Assistant users have operational access to:</p>
     * <ul>
     *   <li>View and search book catalog</li>
     *   <li>Register new books (but cannot deactivate)</li>
     *   <li>Member management (register, edit, suspend/activate)</li>
     *   <li>Loan management (create, return, extend loans)</li>
     * </ul>
     * 
     * <p>Assistants <strong>cannot</strong>:</p>
     * <ul>
     *   <li>Manage system users</li>
     *   <li>Deactivate or permanently delete books</li>
     *   <li>Access reports and exports</li>
     *   <li>Modify system configuration</li>
     * </ul>
     * 
     * @return {@code true} if the current user is an ASSISTANT, {@code false} otherwise
     */
    public boolean isAssistant() { 
        return currentUser != null 
            && currentUser.getRole() != null 
            && UserRole.ASSISTANT.equals(currentUser.getRole()); 
    }
    
    /**
     * Gets the role name of the current user as a display string.
     * 
     * @return the role name (e.g., "ADMIN", "ASSISTANT"), 
     *         or "GUEST" if no user is logged in
     */
    public String getRoleName() {
        if (currentUser == null || currentUser.getRole() == null) {
            return "GUEST";
        }
        return currentUser.getRole().name();
    }
}