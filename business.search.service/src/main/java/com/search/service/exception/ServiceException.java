package com.search.service.exception;

import lombok.Data;

@Data
public class ServiceException extends RuntimeException {
    private String message;
    private String code;

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public ServiceException(String message, String code) {
        this.message = message;
        this.code = code;
    }

    /**
     * Instantiates a new Service exception.
     * @param message the message
     */
    public ServiceException(String message) {
        this.message = message;
        this.code = code;
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public ServiceException(Throwable cause, String message, String code) {
        super(cause);
        this.message = message;
        this.code = code;
    }
}
