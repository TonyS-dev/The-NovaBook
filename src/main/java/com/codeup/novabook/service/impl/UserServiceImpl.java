package com.codeup.novabook.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeup.novabook.domain.User;
import com.codeup.novabook.domain.UserRole;
import com.codeup.novabook.domain.UserStatus;
import com.codeup.novabook.exception.AuthenticationException;
import com.codeup.novabook.exception.DatabaseException;
import com.codeup.novabook.exception.ErrorCode;
import com.codeup.novabook.exception.UserAlreadyExistsException;
import com.codeup.novabook.exception.UserNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.repo.IUserRepository;
import com.codeup.novabook.service.IUserService;
import com.codeup.novabook.util.PasswordUtils;
import com.codeup.novabook.util.ValidationUtils;

/**
 * Service implementation for User management with authentication and business logic.
 */
public class UserServiceImpl implements IUserService {
    
    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
    private final IUserRepository userRepository;
    
    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Optional<User> authenticate(String email, String password) throws AuthenticationException {
        LOGGER.log(Level.INFO, "[POST /api/auth/login] Authenticating user: {0}", email);
        
        try {
            if (ValidationUtils.isEmptyOrNull(email) || ValidationUtils.isEmptyOrNull(password)) {
                throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "[POST /api/auth/login] User not found: {0}", email);
                throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
            }
            
            User user = userOpt.get();
            
            if (!PasswordUtils.checkPassword(password, user.getPassword())) {
                LOGGER.log(Level.WARNING, "[POST /api/auth/login] Invalid credentials");
                throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
            }
            
            if (user.getStatus() != UserStatus.ACTIVE) {
                LOGGER.log(Level.WARNING, "[POST /api/auth/login] User is not active");
                throw new AuthenticationException(ErrorCode.USER_INACTIVE);
            }
            
            LOGGER.log(Level.INFO, "[POST /api/auth/login] User authenticated successfully");
            return Optional.of(user);
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[POST /api/auth/login] Authentication error", e);
            throw new AuthenticationException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void register(String name, String email, String password, String phone) 
            throws ValidationException, UserAlreadyExistsException {
        LOGGER.log(Level.INFO, "[POST /api/users/register] Registering new user: {0}", email);
        
        try {
            // Use centralized validation
            ValidationUtils.validateUser(name, email, password, phone);
            
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException();
            }
            
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(PasswordUtils.hashPassword(password));
            user.setPhone(phone);
            user.setRole(UserRole.ASSISTANT);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            
            LOGGER.log(Level.INFO, "[POST /api/users/register] Applied default role: ASSISTANT, status: ACTIVE");
            
            userRepository.create(user);
            LOGGER.log(Level.INFO, "[POST /api/users/register] User registered successfully");
            
        } catch (UserAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[POST /api/users/register] Registration error", e);
            throw new ValidationException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void adminRegister(String name, String email, String password, String phone, String role, String accessLevel) 
            throws ValidationException, UserAlreadyExistsException {
        LOGGER.log(Level.INFO, "[POST /api/users/admin-register] Admin registering user with role: {0}", role);
        
        try {
            // Use centralized validation
            ValidationUtils.validateUser(name, email, password, phone);
            
            if (userRepository.existsByEmail(email)) {
                throw new UserAlreadyExistsException();
            }
            
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(PasswordUtils.hashPassword(password));
            user.setPhone(phone);
            
            try {
                user.setRole(UserRole.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException(ErrorCode.REQUIRED_FIELD_MISSING);
            }
            
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());
            
            userRepository.create(user);
            LOGGER.log(Level.INFO, "[POST /api/users/admin-register] User registered successfully");
            
        } catch (UserAlreadyExistsException | ValidationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[POST /api/users/admin-register] Registration error", e);
            throw new ValidationException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void rename(User user, String newName) {
        LOGGER.log(Level.INFO, "[PATCH /api/users/{0}/name] Renaming user", user.getId());
        
        try {
            ValidationUtils.validateName(newName);
            user.setName(newName);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.update(user);
            LOGGER.log(Level.INFO, "[PATCH /api/users/{0}/name] Name updated successfully", user.getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/users/name] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void changePassword(User user, String newPassword) {
        LOGGER.log(Level.INFO, "[PATCH /api/users/{0}/password] Changing password", user.getId());
        
        try {
            ValidationUtils.validatePassword(newPassword);
            user.setPassword(PasswordUtils.hashPassword(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.update(user);
            LOGGER.log(Level.INFO, "[PATCH /api/users/{0}/password] Password changed successfully", user.getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/users/password] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public void updateUserByEmail(User user, String email) {
        LOGGER.log(Level.INFO, "[PATCH /api/users/email/{0}] Updating user", email);
        
        try {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                User userToUpdate = existingUser.get();
                userToUpdate.setName(user.getName());
                userToUpdate.setEmail(user.getEmail()); // Update email if changed
                userToUpdate.setPhone(user.getPhone());
                userToUpdate.setRole(user.getRole());
                userToUpdate.setStatus(user.getStatus());
                userToUpdate.setUpdatedAt(LocalDateTime.now());
                userRepository.update(userToUpdate);
                LOGGER.log(Level.INFO, "[PATCH /api/users/email] User updated successfully");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/users/email] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        LOGGER.log(Level.INFO, "[GET /api/users/email/{0}] Fetching user", email);
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/users/email] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<User> list() {
        LOGGER.log(Level.INFO, "[GET /api/users] Fetching all users");
        try {
            List<User> users = userRepository.findAll();
            LOGGER.log(Level.INFO, "[GET /api/users] Found {0} users", users.size());
            return users;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/users] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean userExists(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/users] Error checking email existence", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
}
