/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONObject;

import java.io.IOException;

/**
 * Servlet return information about the User such as their avatar url, name, and other fields in JSON format
 * e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.gcme.json'.
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gcme"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCMeServlet extends GCAbstractServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws IOException {

        GCContext gcContext = getGCContext(request);
        JSONObject responseObject = new JSONObject();

        if (gcContext != null) {
            try {
                responseObject.put("success", false);
                JSONObject jsonObject = getGcContentApi().me(gcContext);
                if (jsonObject != null) {
                    responseObject = jsonObject;
                    responseObject.put("success", true);
                }
            } catch (Exception e) {
                getLOGGER().error("Failed create JSON Object {}", e.getMessage());
            }
        }
        response.getWriter().write(responseObject.toString());
    }
}
