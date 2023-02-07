/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCStatus;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.JSONUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Servlet return list of statuses in JSON format for project ID passed with selector
 * projectId-[number] e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.gcstatuses.projectId-12345.json'.
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"gcstatuses"},
        methods = {HttpConstants.METHOD_GET}
)
public final class GCProjectStatusesServlet extends GCAbstractServlet {
    private static final String JSON_PN_STATUSES = "gcstatuses";

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {

        final GCContext gcContext = getGCContext(request);

        final String[] selectors = request.getRequestPathInfo().getSelectors();
        Integer projectId = 0;
        if (selectors.length == 2) {
            projectId = NumberUtils.toInt(request.getRequestPathInfo().getSelectors()[1].substring(Constants.PROJECT_ID_SELECTOR.length()), 0);
        } else {
            projectId = Stream.of(selectors).distinct()
                    .filter(selector -> selector.startsWith(Constants.PROJECT_ID_SELECTOR))
                    .findFirst()
                    .map(selector -> NumberUtils.toInt(selector.substring(Constants.PROJECT_ID_SELECTOR.length()), 0)).orElse(null);
        }
        if (projectId != null && projectId != 0) {
            try {
                List<GCStatus> gcStatusList = gcContentApi.statusesByProjectId(gcContext, projectId);
                response.getWriter().write(JSONUtil.fromObjectToJsonString(Collections.singletonMap(JSON_PN_STATUSES, gcStatusList)));
                return;
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        response.getWriter().write("{}");
    }
}
