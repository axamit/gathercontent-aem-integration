/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.api.services.GCContentNewApi;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Servlet with References to <code>{@link GCConfiguration}</code> and
 * <code>{@link GCContentApi}</code> services.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Component(componentAbstract = true)
public abstract class GCAbstractServlet extends SlingAllMethodsServlet {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final String REQUEST_PN_GC_USERNAME = "gcUsername";
    private static final String REQUEST_PN_GC_API_KEY = "gcApikey";

    @Reference
    protected GCConfiguration gcConfiguration;

    @Reference
    protected GCContentApi gcContentApi;

    @Reference
    protected GCContentNewApi gcContentNewApi;

    /**
     * Get <code>{@link GCContext}</code> for current cloudservice configuration.
     *
     * @param request <code>{@link SlingHttpServletRequest}</code> object.
     * @return <code>GCContext</code> object with context.
     */
    protected final GCContext getGCContext(final SlingHttpServletRequest request) {
        GCContext gcContext;
        final String gcUsername = request.getParameter(REQUEST_PN_GC_USERNAME);
        final String gcApiKey = request.getParameter(REQUEST_PN_GC_API_KEY);
        if (gcUsername != null && gcApiKey != null) {
            gcContext = GCContext.build(gcUsername, gcApiKey);
        } else {
            final Resource resource = request.getResource();
            gcContext = gcConfiguration.getGCContext(resource);
        }

        return gcContext;
    }

    /**
     * @param request <code>{@link SlingHttpServletRequest}</code> object.
     * @return Account ID for credentials selected in current cloudservice configuration.
     */
    protected final Integer getAccountId(final SlingHttpServletRequest request) {
        final Resource resource = request.getResource();
        return gcConfiguration.getAccountId(resource);
    }
}
