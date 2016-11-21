/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCProject;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Servlet return list of GatherContent projects in JSON format for account ID selected in current cloudservice
 * configuration e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.gcprojects.json'.
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gcprojects"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCProjectsServlet extends GCAbstractServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        GCContext gcContext = getGCContext(request);
        String accountId = getGcConfiguration().getAccountId(request.getResource());
        JSONObject jsonObject = new JSONObject();

        if (gcContext != null) {
            try {
                List<GCProject> gcProjects = getGcContentApi().projects(gcContext, accountId);
                JSONArray jsonArray = new JSONArray();
                for (GCProject gcProject : gcProjects) {
                    JSONObject jsonObjectTemplate = new JSONObject();
                    jsonObjectTemplate.put("text", gcProject.getName());
                    jsonObjectTemplate.put("value", gcProject.getId());
                    jsonObjectTemplate.put("qtip", gcProject.getName());
                    jsonArray.put(jsonObjectTemplate);
                }
                jsonObject.put("gcprojects", jsonArray);
            } catch (Exception e) {
                getLOGGER().error("Failed create JSON Object {}", e.getMessage());
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
