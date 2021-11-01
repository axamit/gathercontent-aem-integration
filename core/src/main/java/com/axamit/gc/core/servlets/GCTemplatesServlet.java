/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCTemplateData;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
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
        final GCContext gcContext = getGCContext(request);
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        Integer projectId = null;
        MappingType mappingType = MappingType.TEMPLATE;
        for (String selector : selectors) {
            if (selector.startsWith(Constants.PROJECT_ID_SELECTOR)) {
                projectId = NumberUtils.toInt(selector.substring(Constants.PROJECT_ID_SELECTOR.length()), 0);
            }
            if (selector.startsWith(Constants.MAPPING_TYPE_SELECTOR)) {
                MappingType mappingTypeFromSelector = MappingType.of(selector.substring(Constants.MAPPING_TYPE_SELECTOR.length()));
                if (mappingTypeFromSelector != null) {
                    mappingType = mappingTypeFromSelector;
                }
            }
        }

        if (projectId == null) {
            projectId = NumberUtils.toInt(request.getResource().adaptTo(ValueMap.class).get(Constants.GC_PROJECT_ID_PN, String.class), 0);
        }

        final JSONObject jsonObject = new JSONObject();
        final JSONArray jsonArray = new JSONArray();

        if (gcContext != null && projectId != 0) {
            switch (mappingType) {
                //TODO
//                case CUSTOM_ITEM:
//                    try {
//                        List<GCItemNew> gcItems = getGcContentNewApi().itemsByProjectId(gcContext, projectId);
//                        for (GCItemNew gcItem : gcItems) {
//                            if (GCItemType.ITEM.equals(gcItem.getItemType()) && gcItem.getTemplateId() == null) {
//                                JSONUtil.addMappingEntry(jsonArray, gcItem.getName(), gcItem.getId(), gcItem.getName());
//                            }
//                        }
//                        jsonObject.put("gctemplates", jsonArray);
//                    } catch (Exception e) {
//                        LOGGER.error("Failed create JSON Object {}", e.getMessage());
//                    }
//                    break;
//                case ENTRY_PARENT:
//                    try {
//                        List<GCItemNew> gcItems = getGcContentNewApi().itemsByProjectId(gcContext, projectId);
//                        for (GCItemNew gcItem : gcItems) {
//                            if (GCItemType.ENTRY_PARENT.equals(gcItem.getItemType())) {
//                                JSONUtil.addMappingEntry(jsonArray, gcItem.getName(), gcItem.getId(), gcItem.getName());
//                            }
//                        }
//                        jsonObject.put("gctemplates", jsonArray);
//                    } catch (Exception e) {
//                        LOGGER.error("Failed create JSON Object {}", e.getMessage());
//                    }
//                    break;
                case TEMPLATE:
                default:
                    try {
                        final List<GCTemplateData> templates = gcContentNewApi.templates(gcContext, projectId);
                        for (GCTemplateData template : templates) {
                            JSONUtil.addMappingEntry(jsonArray, template.getName(), String.valueOf(template.getId()), null);
                        }
                        jsonObject.put("gctemplates", jsonArray);
                    } catch (Exception e) {
                        LOGGER.error("Failed create JSON Object {}", e.getMessage());
                    }
                    break;
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
