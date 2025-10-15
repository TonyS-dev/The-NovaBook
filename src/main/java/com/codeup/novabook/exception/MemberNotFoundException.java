package com.codeup.novabook.exception;

/**
 * Exception thrown when a requested member is not found in the system.
 * <p>
 * This exception is raised when:
 * </p>
 * <ul>
 *   <li>Looking up a member by ID that doesn't exist</li>
 *   <li>Looking up a member by document ID that doesn't exist</li>
 *   <li>Looking up a member by email that doesn't exist</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 1.0
 * @since 1.0
 * @see BusinessException
 */
public class MemberNotFoundException extends BusinessException {
    
    /**
     * Constructs a new MemberNotFoundException with the standard error code.
     */
    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
    
    /**
     * Constructs a new MemberNotFoundException with the specified cause.
     * 
     * @param cause the underlying cause of this exception
     */
    public MemberNotFoundException(Throwable cause) {
        super(ErrorCode.MEMBER_NOT_FOUND, cause);
    }
}
