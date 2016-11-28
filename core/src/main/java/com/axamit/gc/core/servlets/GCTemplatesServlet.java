/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.core.util.Constants;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Servlet return list of GatherContent templates in JSON format for project ID passed with selector
 * projectId-[number] e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.gctemplates.projectId-12345.json'.
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gctemplates"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCTemplatesServlet extends GCAbstractServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        GCContext gcContext = getGCContext(request);
        String[] selectors = request.getRequestPathInfo().getSelectors();
        String projectId = null;
        for (String selector : selectors) {
            if (selector.startsWith(Constants.PROJECT_ID_SELECTOR)) {
                projectId = selector.substring(Constants.PROJECT_ID_SELECTOR.length());
                break;
            }
        }

        if (projectId == null) {
            projectId = request.getResource().adaptTo(ValueMap.class).get("projectId", String.class);
        }

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if (gcContext != null && projectId != null) {
            try {
                List<GCTemplate> templates = getGcContentApi().templates(gcContext, projectId);
                for (GCTemplate template : templates) {
                    JSONObject jsonObjectTemplate = new JSONObject();
                    jsonObjectTemplate.put("text", template.getName());
                    jsonObjectTemplate.put("value", template.getId());
                    jsonObjectTemplate.put("qtip", template.getDescription());
                    jsonArray.put(jsonObjectTemplate);
                }
                jsonObject.put("gctemplates", jsonArray);
            } catch (Exception e) {
                getLOGGER().error("Failed create JSON Object {}", e.getMessage());
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
