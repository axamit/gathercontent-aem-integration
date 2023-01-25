/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.core.util.Constants;
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Servlet which create copy of mapping.
 * Used by URL e.g. '/etc/cloudservices/gathercontent/gathercontent-importer.copy.html'
 * and parameters:
 * resourcePath: '/etc/cloudservices/gathercontent/gathercontent-importer/jcr:content/mapping-list/mapping_14798094'
 * newName: 'copy'
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"copy"},
        methods = {HttpConstants.METHOD_POST, HttpConstants.METHOD_GET}
)
public final class GCCopyMappingServlet extends SlingAllMethodsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCCopyMappingServlet.class);

    private static final String COPY_RESOURCE_PATH_PN = "resourcePath";
    private static final String COPY_RESOURCE_NEW_NAME_PN = "newName";

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {
        final String nodePath = request.getParameter(COPY_RESOURCE_PATH_PN);
        final String newName = request.getParameter(COPY_RESOURCE_NEW_NAME_PN);

        final ResourceResolver resourceResolver = request.getResourceResolver();
        final Resource targetResource = resourceResolver.getResource(nodePath);
        boolean result = false;
        String editLink = null;
        try {
            if (targetResource != null) {
                final Node from = targetResource.adaptTo(Node.class);
                final String newNodeName = from.getName().concat(String.valueOf(System.currentTimeMillis()));
                final Node copiedNode = JcrUtil.copy(from, from.getParent(), newNodeName);
                copiedNode.setProperty(Constants.MAPPING_NAME_PN, newName);
                resourceResolver.commit();
                result = true;
                String mappingType = Constants.MAPPING_IMPORT_SELECTOR;
                if (copiedNode.hasProperty(Constants.EXPORT_OR_IMPORT_MAPPING_TYPE_PN)) {
                    String type = copiedNode.getProperty(Constants.EXPORT_OR_IMPORT_MAPPING_TYPE_PN).getString();
                    if (Constants.MAPPING_TYPE_EXPORT.equals(type)) {
                        mappingType = Constants.MAPPING_EXPORT_SELECTOR;
                    }
                }
                editLink = copiedNode.getPath().replaceAll("/jcr:content/mapping-list/", "." + mappingType + ".mapping-").
                        concat(".html");
            }

            final JSONObject responseObject = new JSONObject();
            responseObject.put("result", result);
            responseObject.put("editLink", editLink);
            response.getWriter().write(responseObject.toString());
        } catch (RepositoryException | JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
