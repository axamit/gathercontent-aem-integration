/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;

import com.axamit.gc.api.GCContext;
import org.apache.sling.api.resource.Resource;

/**
 * The <tt>GCContentApi</tt> interface provides methods to get information about cloudservice instance context config.
 * @author Axamit, gc.support@axamit.com
 */
public interface GCConfiguration {

    /**
     * Get <code>{@link GCContext}</code> of cloudservice config.
     * @param resource Resource of current cloudservice configuration or any of its children resources.
     * @return Context information of cloudservice config.
     */
    GCContext getGCContext(Resource resource);

    /**
     * Get account ID of cloudservice config.
     * @param resource Resource of current cloudservice configuration or any of its children resources.
     * @return Account ID of cloudservice config.
     */
    String getAccountId(Resource resource);
}
