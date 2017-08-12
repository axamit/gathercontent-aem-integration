/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
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
 * Servlet return list of GatherContent accounts in JSON format for credentials selected in current cloudservice
 * configuration e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.gcaccounts.json'
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gcaccounts"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCAccountsServlet extends GCAbstractServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {

        GCContext gcContext = getGCContext(request);
        JSONObject jsonObject = new JSONObject();

        if (gcContext != null) {
            try {

                List<GCAccount> gcAccounts = getGcContentApi().accounts(gcContext);
                JSONArray jsonArray = new JSONArray();
                for (GCAccount gcAccount : gcAccounts) {
                    JSONObject jsonObjectTemplate = new JSONObject();
                    jsonObjectTemplate.put("text", gcAccount.getName());
                    jsonObjectTemplate.put("value", gcAccount.getId());
                    jsonObjectTemplate.put("qtip", gcAccount.getSlug());
                    jsonArray.put(jsonObjectTemplate);
                }
                jsonObject.put("gcaccounts", jsonArray);
            } catch (Exception e) {
                getLOGGER().error("Failed create JSON Object {}", e.getMessage());
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
