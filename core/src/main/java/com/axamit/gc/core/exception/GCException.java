/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.exception;

/**
 * Thrown when any internal operation in application failed to perform.
 * @author Axamit, gc.support@axamit.com
 */
public class GCException extends Exception {

    /**
     * Constructs an GCException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public GCException() {
    }

    /**
     * Constructs an GCException with the specified detail
     * message.  A detail message is a String that describes this particular
     * exception.
     *
     * @param message the String that contains a detailed message.
     */
    public GCException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     * <p>
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link Throwable#getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link Throwable#getCause()} method).  (A <tt>null</tt> value
     *                is permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public GCException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link Throwable#getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public GCException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message, cause, suppression enabled or disabled,
     * and writable stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause. (A null value is permitted, and indicates that the cause is nonexistent
     *                           or unknown.)
     * @param enableSuppression  whether or not suppression is enabled or disabled.
     * @param writableStackTrace whether or not the stack trace should be writable.
     */
    public GCException(final String message, final Throwable cause, final boolean enableSuppression,
                       final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
