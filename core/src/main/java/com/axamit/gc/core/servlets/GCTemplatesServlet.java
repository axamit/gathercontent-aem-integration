/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCItemType;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.util.Constants;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Servlet return list of GatherContent templates in JSON format for project ID passed with selector
 * projectId-[number] e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.gctemplates.projectId-12345.json'.
 *
 * @author Axamit, gc.support@axamit.com
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
        MappingType mappingType = MappingType.TEMPLATE;
        for (String selector : selectors) {
            if (selector.startsWith(Constants.PROJECT_ID_SELECTOR)) {
                projectId = selector.substring(Constants.PROJECT_ID_SELECTOR.length());
            }
            if (selector.startsWith(Constants.MAPPING_TYPE_SELECTOR)) {
                MappingType mappingTypeFromSelector =
                        MappingType.of(selector.substring(Constants.MAPPING_TYPE_SELECTOR.length()));
                if (mappingTypeFromSelector != null) {
                    mappingType = mappingTypeFromSelector;
                }
            }
        }

        if (projectId == null) {
            projectId = request.getResource().adaptTo(ValueMap.class).get(Constants.GC_PROJECT_ID_PN, String.class);
        }

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if (gcContext != null && projectId != null) {
            switch (mappingType) {
                case CUSTOM_ITEM:
                    try {
                        List<GCItem> gcItems = getGcContentApi().itemsByProjectId(gcContext, projectId);
                        for (GCItem gcItem : gcItems) {
                            if (GCItemType.ITEM.equals(gcItem.getItemType()) && gcItem.getTemplateId() == null) {
                                addMappingEntry(jsonArray, gcItem.getName(), gcItem.getId(), gcItem.getName());
                            }
                        }
                        jsonObject.put("gctemplates", jsonArray);
                    } catch (Exception e) {
                        getLOGGER().error("Failed create JSON Object {}", e.getMessage());
                    }
                    break;
                case ENTRY_PARENT:
                    try {
                        List<GCItem> gcItems = getGcContentApi().itemsByProjectId(gcContext, projectId);
                        for (GCItem gcItem : gcItems) {
                            if (GCItemType.ENTRY_PARENT.equals(gcItem.getItemType())) {
                                addMappingEntry(jsonArray, gcItem.getName(), gcItem.getId(), gcItem.getName());
                            }
                        }
                        jsonObject.put("gctemplates", jsonArray);
                    } catch (Exception e) {
                        getLOGGER().error("Failed create JSON Object {}", e.getMessage());
                    }
                    break;
                case TEMPLATE:
                default:
                    try {
                        List<GCTemplate> templates = getGcContentApi().templates(gcContext, projectId);
                        for (GCTemplate template : templates) {
                            addMappingEntry(jsonArray, template.getName(), template.getId(), template.getDescription());
                        }
                        jsonObject.put("gctemplates", jsonArray);
                    } catch (Exception e) {
                        getLOGGER().error("Failed create JSON Object {}", e.getMessage());
                    }
                    break;
            }
        }
        response.getWriter().write(jsonObject.toString());
    }

    private void addMappingEntry(JSONArray jsonArray, String text, String value, String qtip) throws JSONException {
        JSONObject jsonObjectTemplate = new JSONObject();
        jsonObjectTemplate.put("text", text);
        jsonObjectTemplate.put("value", value);
        jsonObjectTemplate.put("qtip", qtip);
        jsonArray.put(jsonObjectTemplate);
    }
}
