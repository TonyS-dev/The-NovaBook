package com.codeup.novabook.service;

import com.codeup.novabook.domain.User;
import com.codeup.novabook.domain.UserRole;
import com.codeup.novabook.domain.UserStatus;
import com.codeup.novabook.exception.AuthenticationException;
import com.codeup.novabook.exception.UserAlreadyExistsException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.repo.IUserRepository;
import com.codeup.novabook.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService implementation.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>User registration with validation</li>
 *   <li>Admin registration with roles</li>
 *   <li>Authentication with BCrypt password verification</li>
 *   <li>Email uniqueness checks</li>
 *   <li>Password hashing</li>
 *   <li>Input validation (empty fields, invalid emails, weak passwords)</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 */
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    private IUserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository);
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should register user with valid inputs")
    void testRegisterUserWithValidInputs() throws Exception {
        // Arrange
        String name = "Juan Pérez";
        String email = "juan@email.com";
        String password = "SecurePass123!";
        String phone = "1234567890";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        userService.register(name, email, password, phone);

        // Assert
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).create(argThat(user ->
            user.getName().equals(name) &&
            user.getEmail().equals(email) &&
            user.getPhone().equals(phone) &&
            user.getStatus() == UserStatus.ACTIVE &&
            BCrypt.checkpw(password, user.getPassword())
        ));
    }

    @Test
    @DisplayName("Should throw ValidationException when name is empty")
    void testRegisterUserWithEmptyName() {
        // Arrange
        String name = "";
        String email = "test@email.com";
        String password = "SecurePass123!";
        String phone = "1234567890";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () ->
            userService.register(name, email, password, phone)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("required") || 
                   exception.getMessage().toLowerCase().contains("missing"));
    }

    @Test
    @DisplayName("Should throw ValidationException when name contains only digits")
    void testRegisterUserWithNumericName() {
        // Arrange
        String name = "12345";
        String email = "test@email.com";
        String password = "SecurePass123!";
        String phone = "1234567890";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () ->
            userService.register(name, email, password, phone)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("name") || 
                   exception.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    @DisplayName("Should throw ValidationException when email is invalid")
    void testRegisterUserWithInvalidEmail() {
        // Arrange
        String name = "Juan Pérez";
        String email = "invalid-email";
        String password = "SecurePass123!";
        String phone = "1234567890";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () ->
            userService.register(name, email, password, phone)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("email"));
    }

    @Test
    @DisplayName("Should throw ValidationException when password is too short")
    void testRegisterUserWithShortPassword() {
        // Arrange
        String name = "Juan Pérez";
        String email = "juan@email.com";
        String password = "Pass1!";
        String phone = "1234567890";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () ->
            userService.register(name, email, password, phone)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("password") || 
                   exception.getMessage().toLowerCase().contains("weak"));
    }

    @Test
    @DisplayName("Should throw ValidationException when phone is invalid")
    void testRegisterUserWithInvalidPhone() {
        // Arrange
        String name = "Juan Pérez";
        String email = "juan@email.com";
        String password = "SecurePass123!";
        String phone = "123ABC";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () ->
            userService.register(name, email, password, phone)
        );
        assertTrue(exception.getMessage().toLowerCase().contains("phone"));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email already exists")
    void testRegisterUserWithDuplicateEmail() {
        // Arrange
        String name = "Juan Pérez";
        String email = "existing@email.com";
        String password = "SecurePass123!";
        String phone = "1234567890";
        
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
            userService.register(name, email, password, phone)
        );
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, never()).create(any());
    }

    // ==================== ADMIN REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should register admin user with ADMIN role")
    void testAdminRegisterWithAdminRole() throws Exception {
        // Arrange
        String name = "Admin User";
        String email = "admin@mail.com";
        String password = "Admin123!";
        String phone = "9876543210";
        String role = "ADMIN";
        String accessLevel = "MANAGE";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        userService.adminRegister(name, email, password, phone, role, accessLevel);

        // Assert
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).create(argThat(user ->
            user.getName().equals(name) &&
            user.getEmail().equals(email) &&
            user.getPhone().equals(phone) &&
            user.getRole() == UserRole.ADMIN &&
            user.getStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("Should register assistant user with ASSISTANT role")
    void testAdminRegisterWithAssistantRole() throws Exception {
        // Arrange
        String name = "Assistant User";
        String email = "assistant@mail.com";
        String password = "Assist123!";
        String phone = "5551234567";
        String role = "ASSISTANT";
        String accessLevel = "READ_WRITE";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        userService.adminRegister(name, email, password, phone, role, accessLevel);

        // Assert
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).create(argThat(user ->
            user.getName().equals(name) &&
            user.getEmail().equals(email) &&
            user.getPhone().equals(phone) &&
            user.getRole() == UserRole.ASSISTANT &&
            user.getStatus() == UserStatus.ACTIVE
        ));
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @DisplayName("Should authenticate user with correct credentials")
    void testAuthenticateWithCorrectCredentials() throws Exception {
        // Arrange
        String email = "user@email.com";
        String password = "CorrectPass123!";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.authenticate(email, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw AuthenticationException with incorrect password")
    void testAuthenticateWithIncorrectPassword() {
        // Arrange
        String email = "user@email.com";
        String correctPassword = "CorrectPass123!";
        String incorrectPassword = "WrongPass456!";
        String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt());
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setStatus(UserStatus.ACTIVE);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(AuthenticationException.class, () ->
            userService.authenticate(email, incorrectPassword)
        );
    }

    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testAuthenticateWithNonExistentUser() {
        // Arrange
        String email = "nonexistent@email.com";
        String password = "SomePass123!";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () ->
            userService.authenticate(email, password)
        );
    }

    // ==================== USER EXISTS TESTS ====================

    @Test
    @DisplayName("Should return true when user exists")
    void testUserExistsReturnsTrueWhenExists() {
        // Arrange
        String email = "existing@email.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = userService.userExists(email);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void testUserExistsReturnsFalseWhenNotExists() {
        // Arrange
        String email = "nonexistent@email.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = userService.userExists(email);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail(email);
    }

    // ==================== LIST USERS TEST ====================

    @Test
    @DisplayName("Should return list of all users")
    void testListReturnsAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1);
        user1.setName("User 1");
        user1.setEmail("user1@email.com");
        
        User user2 = new User();
        user2.setId(2);
        user2.setName("User 2");
        user2.setEmail("user2@email.com");
        
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.list();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testListReturnsEmptyListWhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<User> result = userService.list();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== FIND BY EMAIL TEST ====================

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmailReturnsUser() {
        // Arrange
        String email = "test@email.com";
        User user = new User();
        user.setEmail(email);
        user.setName("Test User");
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty Optional when user not found by email")
    void testFindByEmailReturnsEmptyWhenNotFound() {
        // Arrange
        String email = "notfound@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isEmpty());
    }
}
