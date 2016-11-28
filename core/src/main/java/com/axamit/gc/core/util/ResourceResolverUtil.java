/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import com.google.common.collect.ImmutableMap;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The <code>ResourceResolverUtil</code> is an utility class for creating new <code>{@link ResourceResolver}</code>,
 * used for getting <code>{@link ResourceResolver}</code> with specified authentication information across application.
 */
public enum ResourceResolverUtil {
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONUtil.class);

    /**
     * @param resourceResolverFactory Reference to <code>resourceResolverFactory</code> service.
     * @param subservice              Name of the authentication information property providing the Subservice Name for
     *                                the service requesting a resource resolver.
     * @return a new <code>{@link ResourceResolver}</code> with privileges assigned to the service provided by the
     * calling bundle using <code>{@link ResourceResolverFactory#getServiceResourceResolver(Map)}</code>,
     * and as a legacy fallback administrative {@link ResourceResolver} using
     * <code>{@link ResourceResolverFactory#getAdministrativeResourceResolver(Map)}</code>.
     * @throws LoginException If an error occurs during creating the new ResourceResolver for the service represented
     *                        by the calling bundle.
     */
    public static ResourceResolver getResourceResolver(final ResourceResolverFactory resourceResolverFactory,
                                                       final String subservice) throws LoginException {
        try {
            Map<String, Object> paramMap =
                    ImmutableMap.<String, Object>of(ResourceResolverFactory.SUBSERVICE, subservice);
            final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(paramMap);
            if (resourceResolver != null) {
                return resourceResolver;
            }
        } catch (LoginException e) {
            LOGGER.error(e.getMessage(), e);
        }
        // Legacy fallback
        return resourceResolverFactory.getAdministrativeResourceResolver(null);
    }
}
