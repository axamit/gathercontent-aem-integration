/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.core.services.plugins.GCPluginManager;
import com.axamit.gc.core.util.JSONUtil;
import org.apache.felix.scr.annotations.Reference;
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
 * Servlet return list of registered GC import Plugins in JSON format.
 * '/etc/cloudservices/gathercontent/gathercontent-importer.gcplugins.json'
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gcplugins"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCPluginsServlet extends GCAbstractServlet {
    private static final String JSON_PN_PLUGINS = "gcplugins";

    @Reference
    private GCPluginManager gcPluginManager;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        List<String> registeredPluginsPIDs = gcPluginManager.getRegisteredPluginsPIDs();

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if (registeredPluginsPIDs != null) {
            try {
                for (String pluginPID : registeredPluginsPIDs) {
                    JSONUtil.addMappingEntry(jsonArray, pluginPID, pluginPID, pluginPID);
                }
                jsonObject.put(JSON_PN_PLUGINS, jsonArray);
            } catch (Exception e) {
                LOGGER.error("Failed create JSON Object: {}", e.getMessage());
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
