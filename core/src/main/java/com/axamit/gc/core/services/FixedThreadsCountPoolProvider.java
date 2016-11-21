/*
 * Axamit, gc.support@axamit.com
 * @author Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread pool service implementation.
 * @since 1.0.13 (25.10.2016)
 * @author Axamit, gc.support@axamit.com
 */
@Service
@Component(label = "GatherContent: Fixed threads count pool provider",
        name = "Fixed threads count pool provider",
        description = "Set here count of threads in your thread pool",
        immediate = true, metatype = true)
public final class FixedThreadsCountPoolProvider implements ThreadsPoolProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedThreadsCountPoolProvider.class);

    private static final int MIN_POOL_CAPACITY = 1;
    private static final int MAX_POOL_CAPACITY = 200;
    private static final String PROPERTY_NAME = "count of pool threads";
    @Property(name = PROPERTY_NAME, description = "How many threads be contained in pool, min: " + MIN_POOL_CAPACITY + " , max: " + MAX_POOL_CAPACITY,
            intValue = FixedThreadsCountPoolProvider.DEFAULT_POOL_SIZE)
    private static final String THREADS_POOL_SIZE = "threads.pool.size";


    private static final int DEFAULT_POOL_SIZE = 20;
    private final Lock threadPoolLock = new ReentrantLock();
    private ExecutorService pool;
    private int currentPoolSize = DEFAULT_POOL_SIZE;

    /**
     * Activates or updates the service.
     *
     * @param properties configuration properties
     */
    @Activate
    @Modified
    public void activate(Map<String, Object> properties) {
        final int possibleCurrentPoolSize = Math.max(Math.min(
                PropertiesUtil.toInteger(properties.get(THREADS_POOL_SIZE), DEFAULT_POOL_SIZE),
                MAX_POOL_CAPACITY), MIN_POOL_CAPACITY);
        try {
            threadPoolLock.lock();
            if (possibleCurrentPoolSize != currentPoolSize || pool == null) {
                reConfigureThreadPool(possibleCurrentPoolSize);
            }
        } finally {
            threadPoolLock.unlock();
        }

    }

    /**
     * Deactivates the service.
     */
    @Deactivate
    public void deactivate() {
        try {
            threadPoolLock.lock();
            if (pool != null && !pool.isShutdown()) {
                pool.shutdown();
            }
        } finally {
            threadPoolLock.unlock();
        }
    }

    private void reConfigureThreadPool(int possibleCurrentPoolSize) {
        shutdownPreviousPool();
        createNewPool(possibleCurrentPoolSize);
    }

    private void createNewPool(int possibleCurrentPoolSize) {
        currentPoolSize = possibleCurrentPoolSize;
        pool = Executors.newFixedThreadPool(possibleCurrentPoolSize);
    }

    private void shutdownPreviousPool() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ThreadsPoolProvider executeTasks(Collection<Runnable> tasks) {
        try {
            threadPoolLock.lock();
            if (pool == null) {
                reConfigureThreadPool(DEFAULT_POOL_SIZE);
            }
            if (tasks != null && !tasks.isEmpty()) {
                for (Runnable task : tasks) {
                    pool.execute(task);
                }
            } else {
                LOGGER.warn("No tasks {}", tasks);
            }
            return this;
        } finally {
            threadPoolLock.unlock();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> callableCollection)
            throws InterruptedException {
        try {
            threadPoolLock.lock();
            if (pool == null) {
                reConfigureThreadPool(DEFAULT_POOL_SIZE);
            }
            if (callableCollection != null && !callableCollection.isEmpty()) {
                return pool.invokeAll(callableCollection);
            } else {
                LOGGER.warn("No tasks {}", callableCollection);
                return Collections.emptyList();
            }
        } finally {
            threadPoolLock.unlock();
        }
    }
}
