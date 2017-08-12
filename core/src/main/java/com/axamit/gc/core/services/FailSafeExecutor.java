/*
 * Axamit, gc.support@axamit.com
 * @author Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import java.util.concurrent.Callable;

/**
 * The <tt>GCItemCreator</tt> interface provides methods to execute operations with retries
 * if repository exception occurs.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface FailSafeExecutor {
    /**
     * Execute operations with retries if repository exception occurs.
     *
     * @param task Callable for execution with retries.
     * @param <E>  The result type of method.
     * @return Return object.
     */
    <E> E executeWithRetries(Callable<E> task);
}
