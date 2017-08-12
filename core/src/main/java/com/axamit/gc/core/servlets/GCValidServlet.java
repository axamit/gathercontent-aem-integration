/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.apache.sling.api.resource.Resource.RESOURCE_TYPE_NON_EXISTING;

/**
 * Servlet return 'valid' if resource exist for the requested path, 'no-valid' otherwise
 * e.g. '/content/geometrixx/en.valid.html'.
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"valid"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCValidServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {

        Resource resource = request.getResource();

        if (!RESOURCE_TYPE_NON_EXISTING.equals(resource.getResourceType())) {
            response.getWriter().write("valid");
        } else {
            response.getWriter().write("no-valid");
        }
    }
}
