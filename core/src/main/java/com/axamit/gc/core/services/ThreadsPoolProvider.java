/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Thread pool service.
 *
 * @since 1.0.13 (25.10.2016)
 * @author Axamit, gc.support@axamit.com
 */
public interface ThreadsPoolProvider {
    /**
     * Adds tasks into thread pool and executes them.
     * Lockable, null-safe
     *
     * @param tasks tasks to be executed
     * @return himself
     */
    ThreadsPoolProvider executeTasks(Collection<Runnable> tasks);

    /**
     * Executes the given tasks, returning a list of Futures holding
     * their status and results when all complete.
     * Lockable, null-safe
     *
     * @param callableCollection Collection of callables to be executed
     * @param <T>                the type of the Feature.
     * @return List of Futures.
     * @throws InterruptedException if interrupted while waiting, in
     *                              which case unfinished tasks are cancelled.
     */
    <T> List<Future<T>> invokeAll(Collection<Callable<T>> callableCollection) throws InterruptedException;
}
