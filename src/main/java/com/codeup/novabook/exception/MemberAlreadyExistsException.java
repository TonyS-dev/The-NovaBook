package com.codeup.novabook.exception;

/**
 * Exception thrown when attempting to create a member that already exists.
 * <p>
 * This exception is raised when:
 * </p>
 * <ul>
 *   <li>A member with the same document ID already exists</li>
 *   <li>A member with the same email address already exists</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see BusinessException
 */
public class MemberAlreadyExistsException extends BusinessException {
    
    /**
     * Constructs a new MemberAlreadyExistsException with the standard error code.
     */
    public MemberAlreadyExistsException() {
        super(ErrorCode.MEMBER_ALREADY_EXISTS);
    }
    
    /**
     * Constructs a new MemberAlreadyExistsException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public MemberAlreadyExistsException(Throwable cause) {
        super(ErrorCode.MEMBER_ALREADY_EXISTS, cause);
    }
}
