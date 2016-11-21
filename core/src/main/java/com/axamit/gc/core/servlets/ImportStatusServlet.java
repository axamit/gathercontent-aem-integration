/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.services.ImportService;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
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

/**
 * Servlet return current status of import process by job ID in JSON format e.g.
 * '/etc/cloudservices/gathercontent/gathercontent-importer.importstatus.json?
 * jobId=2016/9/16/17/25/4670e235-72ea-4643-a8c4-6b336f0f7c22_112'.
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"importstatus"},
        methods = {HttpConstants.METHOD_GET}
)
public final class ImportStatusServlet extends GCAbstractServlet {

    public static final String JOB_ID = "jobId";


    @Reference
    private ImportService importService;

    @Reference
    private JobManager jobManager;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        try {
            String jobId = request.getParameter(JOB_ID);
            if (jobId == null || jobId.isEmpty()) {
                getLOGGER().error("{} parameter is empty", JOB_ID);
                jsonObject.put("errorString", Constants.UNEXPECTED_ERROR_STRING);
                response.getWriter().write(jsonObject.toString());
                return;
            }
            ResourceResolver resourceResolver = request.getResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = pageManager.getContainingPage(request.getResource());
            String pagePath = page.getPath();
            Job job = jobManager.getJobById(jobId);
            try {
                ImportModel importModel = importService.getImportStatus(pagePath, jobId);
                if (importModel != null) {
                    String responseString = JSONUtil.fromObjectToJsonString(importModel);
                    jsonObject.put("importData", responseString);
                    response.getWriter().write(jsonObject.toString());
                    return;
                }
            } catch (GCException e) {
                getLOGGER().error("Failed get Import Status {}", e.getMessage());
            }
            if (job != null) {
                switch (job.getJobState()) {
                    case GIVEN_UP:
                        jsonObject.put("errorString", Constants.UNEXPECTED_ERROR_STRING
                                + " Maximum number of retries (" + job.getNumberOfRetries() + ") exceeded.");
                        break;
                    default:
                        String status = "Import Job is in progress...";
                        if (job.getRetryCount() > 0) {
                            status += String.format(" Retry (%s/%s)", job.getRetryCount(), job.getNumberOfRetries());
                        }
                        jsonObject.put("statusString", status);
                }
            } else {
                getLOGGER().error("No Sling Job with ID '{}' was found", jobId);
                jsonObject.put("errorString", Constants.UNEXPECTED_ERROR_STRING);
            }
        } catch (JSONException e) {
            getLOGGER().error(e.getMessage(), e);
        }
        response.getWriter().write(jsonObject.toString());
    }
}
