/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;


import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.FieldMappingProperties;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Servlet return list of mappings in JSON format for project ID passed and applicable for AEM Page
 * e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.pagemappings.json'
 * and parameters:
 * pagePath: '/content/geometrixx/en'
 * projectId: '78496'
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"pagemappings"},
        methods = {HttpConstants.METHOD_POST}
)
public class PageMappingsServlet extends GCAbstractServlet {

    private static final String PAGE_PATH_PN = "pagePath";
    private static final String KEY_NAME = "matchedProps";
    public static final int LIST_CAPACITY = 40;

    @Override
    protected final void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {

        ResourceResolver resourceResolver = request.getResourceResolver();
        String pagePath = request.getParameter(PAGE_PATH_PN);
        JSONArray responseJSON;
        List<JSONObject> sortedJSONArray = new ArrayList<>(LIST_CAPACITY);
        //get GCUtil all mappings by project
        Map<String, Map<String, String>> mappings =
                GCUtil.getProjectMappings(request.getResource(),
                        Integer.parseInt(request.getParameter(Constants.GC_PROJECT_ID_PN)), true);

        //iterate over mappings and compare them with chosen page
        for (Map.Entry<String, Map<String, String>> mapping : mappings.entrySet()) {
            try {
                String mapperStr = mapping.getValue().get(Constants.MAPPING_MAPPER_STR);
                List<String> mappedProperties = new LinkedList<>();

                //parsing mapperStr property of Mapping
                //add collect non-empty(mapped) properties
                Map<String, FieldMappingProperties> mappingMap = JSONUtil.fromJsonToMappingMap(mapperStr);
                for (Map.Entry<String, FieldMappingProperties> mapEntry : mappingMap.entrySet()) {
                    mappedProperties.addAll(mapEntry.getValue().getPath());
                }

                //find matches of mapped properties in mapping and page properties
                int matchedProperties = 0;
                for (String prop : mappedProperties) {
                    Resource property = resourceResolver.getResource(GCStringUtil.appendNewLevelToPath(pagePath, prop));
                    if (property != null) {
                        matchedProperties++;
                    }
                }

                //setting result
                if (matchedProperties > 0) {
                    // responseJSON.put(
                    sortedJSONArray.add(new JSONObject().put(Constants.MAPPING_NAME_PN, mapping.getKey())
                            .put(KEY_NAME, matchedProperties)
                            .put(Constants.GC_TEMPLATE_NAME_PN, mapping.getValue().get(Constants.GC_TEMPLATE_NAME_PN))
                            .put(Constants.GC_TEMPLATE_ID_PN, mapping.getValue().get(Constants.GC_TEMPLATE_ID_PN))
                            .put(Constants.GC_MAPPING_PATH, mapping.getValue().get(Constants.GC_MAPPING_PATH)));
                }
            } catch (JSONException | GCException e) {
                getLOGGER().error(e.getMessage(), e);
            }
        }
        Collections.sort(sortedJSONArray, new Comparator<JSONObject>() {
            @Override
            public int compare(final JSONObject o1, final JSONObject o2) {
                int mp1 = 0;
                int mp2 = 0;
                try {
                    mp1 = o1.getInt(KEY_NAME);
                    mp2 = o2.getInt(KEY_NAME);
                } catch (JSONException e) {
                    getLOGGER().error(e.getMessage(), e);
                }
                return mp2 - mp1;
            }

        });
        try {
            responseJSON = new JSONArray(sortedJSONArray.toString());
            response.getWriter().print(responseJSON);
        } catch (JSONException e) {
            getLOGGER().error(e.getMessage(), e);
        }
    }
}

