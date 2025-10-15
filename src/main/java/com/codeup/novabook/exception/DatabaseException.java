package com.codeup.novabook.exception;

/**
 * Exception thrown when database operations fail.
 * <p>
 * This exception represents infrastructure-level database errors such as
 * connection failures, transaction rollbacks, or SQL execution errors.
 * It extends {@link InfrastructureException} as database issues are
 * typically technical problems requiring system-level resolution.
 * </p>
 * 
 * @author TonyS-dev/Antonio Santiago
 * @version 2.0
 * @since 1.0
 */
public class DatabaseException extends InfrastructureException {
    
    /**
     * Constructs a new DatabaseException with the general database error code.
     */
    public DatabaseException() {
        super(ErrorCode.DATABASE_ERROR);
    }

    /**
     * Constructs a new DatabaseException with a specific error code.
     * 
     * @param errorCode the specific database error code
     */
    public DatabaseException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new DatabaseException with the general database error code and cause.
     * 
     * @param cause the underlying cause of this exception (typically SQLException)
     */
    public DatabaseException(Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, cause);
    }

    /**
     * Constructs a new DatabaseException with a specific error code and cause.
     * 
     * @param errorCode the specific database error code
     * @param cause the underlying cause of this exception
     */
    public DatabaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    // Convenience factory methods
    
    /**
     * Creates a DatabaseException for connection failures.
     * 
     * @param cause the underlying connection failure cause
     * @return DatabaseException with connection failed error code
     */
    public static DatabaseException connectionFailed(Throwable cause) {
        return new DatabaseException(ErrorCode.DATABASE_CONNECTION_FAILED, cause);
    }

    /**
     * Creates a DatabaseException for transaction failures.
     * 
     * @param cause the underlying transaction failure cause
     * @return DatabaseException with transaction failed error code
     */
    public static DatabaseException transactionFailed(Throwable cause) {
        return new DatabaseException(ErrorCode.TRANSACTION_FAILED, cause);
    }

    /**
     * Creates a DatabaseException for data integrity violations.
     * 
     * @param cause the underlying integrity violation cause
     * @return DatabaseException with data integrity violation error code
     */
    public static DatabaseException integrityViolation(Throwable cause) {
        return new DatabaseException(ErrorCode.DATA_INTEGRITY_VIOLATION, cause);
    }

    /**
     * Creates a DatabaseException for general database operations.
     * 
     * @param cause the underlying database operation failure cause
     * @return DatabaseException with general database error code
     */
    public static DatabaseException forOperation(Throwable cause) {
        return new DatabaseException(ErrorCode.DATABASE_ERROR, cause);
    }
}