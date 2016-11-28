/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.core.jobs.ImportJobConsumer;
import com.axamit.gc.core.util.Constants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.util.Base64;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet for receiving list items to import and start import job.
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gcimporter"},
        methods = {HttpConstants.METHOD_POST}
)
public final class GCImporterServlet extends GCAbstractServlet {

    public static final String UPDATE_SELECTOR = "update";
    public static final String SINGLE_SPACE_IN_ENCODED_BASE_64 = "\\s";
    public static final String PLUS_SYMBOL = "+";
    public static final String DATA_REQUEST_PARAMETER = "data";

    @Reference
    private JobManager jobManager;

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        //So, this is really non-trivial solution, please let me explain
        //In 'data' field expected valid json structure with info about imported pages
        //At client-side: JSON.stringify(someObject) does not guarantee any escaping/encoding;
        //Let's use base64 for encoding any symbols, and recover '+' symbols encoded by encodeURIComponent under
        // the jquery hood (base64 does not contains whitespaces)
        JSONObject jsonObject = new JSONObject();
        try {
            String data = Base64.decode(request.getRequestParameter(DATA_REQUEST_PARAMETER).getString()
                    .replaceAll(SINGLE_SPACE_IN_ENCODED_BASE_64, PLUS_SYMBOL));
            Map<String, Object> params = new HashMap<>();
            String[] selectors = request.getRequestPathInfo().getSelectors();
            params.put(ImportJobConsumer.JOB_PARAM_UPDATE_FLAG, false);
            for (String selector : selectors) {
                if (UPDATE_SELECTOR.equals(selector)) {
                    params.put(ImportJobConsumer.JOB_PARAM_UPDATE_FLAG, true);
                    break;
                }
            }
            params.put(ImportJobConsumer.JOB_PARAM_DATA, data);
            GCContext gcContext = getGCContext(request);
            params.put(ImportJobConsumer.JOB_PARAM_CONTEXT_KEY, gcContext.getApikey());
            params.put(ImportJobConsumer.JOB_PARAM_CONTEXT_USERNAME, gcContext.getUsername());
            params.put(ImportJobConsumer.JOB_PARAM_ACCOUNT_ID, getAccountId(request));
            ResourceResolver resourceResolver = request.getResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = pageManager.getContainingPage(request.getResource());
            params.put(ImportJobConsumer.JOB_PARAM_STATUS_STORE, page.getPath());
            Job importJob = jobManager.addJob(ImportJobConsumer.JOB_TOPIC, params);
            if (importJob != null) {
                jsonObject.put("jobId", importJob.getId());
            }
        } catch (Exception e) {
            getLOGGER().error("Error during processing of request with information about items to import"
                    + "/Sling Job creation", e);
            try {
                jsonObject.put("errorString", Constants.UNEXPECTED_ERROR_STRING);
            } catch (JSONException e1) {
                getLOGGER().error("Failed create JSON Object", e1);
            }
        }
        response.getWriter().write(jsonObject.toString());
    }
}
