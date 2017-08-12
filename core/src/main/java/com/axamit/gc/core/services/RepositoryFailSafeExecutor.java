/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.commons.osgi.PropertiesUtil;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Service retry operation if repository exception occurs.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service
@Component(label = "GatherContent: Repository fail safe executor",
        name = "Repository fail safe executor",
        description = "Set here count of retries",
        immediate = true, metatype = true)
public class RepositoryFailSafeExecutor implements FailSafeExecutor {
    private static final int MIN_RETRIES_COUNT = 1;
    private static final int MAX_RETRIES_COUNT = 10;
    private static final int DEFAULT_RETRIES_COUNT = 3;

    private static final int MIN_DELAY_BETWEEN_RETRIES = 1;
    private static final int MAX_DELAY_BETWEEN_RETRIES = 10;
    private static final int DEFAULT_DELAY_BETWEEN_RETRIES = 1;

    @Property(name = "count of retries", description = "How many retries will be made: "
            + MIN_RETRIES_COUNT + " , max: " + MAX_RETRIES_COUNT,
            intValue = DEFAULT_RETRIES_COUNT)
    private static final String RETRY_COUNT = "count.of.retries";

    @Property(name = "delay before next try", description = "How many seconds thread will wait after unsuccessful retry for a next try: "
            + MIN_DELAY_BETWEEN_RETRIES + " , max: " + MAX_DELAY_BETWEEN_RETRIES,
            intValue = DEFAULT_DELAY_BETWEEN_RETRIES)
    private static final String RETRY_DELAY = "delay.between.retries";

    private RetryPolicy retryPolicy = createRetryPolicy(DEFAULT_RETRIES_COUNT, DEFAULT_DELAY_BETWEEN_RETRIES);

    /**
     * Service activation method.
     *
     * @param properties configuration properties
     */
    @Activate
    @Modified
    protected void activate(Map<String, Object> properties) {
        final int retryCount = Math.max(Math.min(
                PropertiesUtil.toInteger(properties.get(RETRY_COUNT), DEFAULT_RETRIES_COUNT),
                MAX_RETRIES_COUNT), MIN_RETRIES_COUNT);

        final int delayBetweenRetries = Math.max(Math.min(
                PropertiesUtil.toInteger(properties.get(RETRY_DELAY), DEFAULT_DELAY_BETWEEN_RETRIES),
                MAX_DELAY_BETWEEN_RETRIES), MIN_DELAY_BETWEEN_RETRIES);
        retryPolicy = createRetryPolicy(retryCount, delayBetweenRetries);

    }

    private RetryPolicy createRetryPolicy(int countOfRetries, int delayBetweenRetries) {
        return new RetryPolicy()
                .retryOn(RepositoryException.class, PersistenceException.class)
                .withDelay(delayBetweenRetries, TimeUnit.SECONDS)
                .withMaxRetries(countOfRetries);
    }

    @Override
    public <E> E executeWithRetries(Callable<E> task) {
        //There are no issues with multithreading and shared retryPolicy
        return Failsafe.with(retryPolicy).get(task);
    }
}
