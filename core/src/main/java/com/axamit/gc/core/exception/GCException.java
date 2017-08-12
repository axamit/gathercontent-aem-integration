/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.exception;

/**
 * Thrown when any internal operation in application failed to perform.
 *
 * @author Axamit, gc.support@axamit.com
 */
public class GCException extends Exception {

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

}
