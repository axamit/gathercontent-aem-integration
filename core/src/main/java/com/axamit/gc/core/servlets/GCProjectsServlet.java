/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    private static final String JSON_PN_PROJECTS = "gcprojects";

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        final GCContext gcContext = getGCContext(request);
        final Integer accountId = gcConfiguration.getAccountId(request.getResource());
        final JSONObject jsonObject = new JSONObject();
        final String[] selectors = request.getRequestPathInfo().getSelectors();

        boolean mapped = false;
        String side = Constants.MAPPING_TYPE_IMPORT;
        Set<Integer> mappedProjectsIds = null;
        for (String selector : selectors) {
            if (Constants.MAPPED_ITEMS_SELECTOR.equals(selector)) {
                mapped = true;
            }
            if (Constants.MAPPING_TYPE_EXPORT.equals(selector)) {
                side = Constants.MAPPING_TYPE_EXPORT;
            }
        }

        if (mapped) {
            mappedProjectsIds = GCUtil.getMappedProjectsIds(request.getResource(), side);
        }

        if (gcContext != null) {
            try {
                final List<GCProject> gcProjects = gcContentApi.projects(gcContext, accountId);
                final JSONArray jsonArray = new JSONArray();
                for (GCProject gcProject : gcProjects) {
                    if (!mapped || mappedProjectsIds != null && mappedProjectsIds.contains(gcProject.getId())) {
                        JSONUtil.addMappingEntry(jsonArray, gcProject.getName(), String.valueOf(gcProject.getId()), null);
                    }
                }
                jsonObject.put(JSON_PN_PROJECTS, jsonArray);
            } catch (Exception e) {
                LOGGER.error("Failed create JSON Object {}", e.getMessage());
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
