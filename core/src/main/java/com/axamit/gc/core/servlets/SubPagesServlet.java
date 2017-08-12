/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;


import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.helpers.PageWrap;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.NameConstants;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Servlet for obtaining items suitable for export e.g 'cq:Page', 'sling:Folder', 'sling:OrderedFolder'.
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"subpages"},
        methods = {HttpConstants.METHOD_POST}
)
public class SubPagesServlet extends SlingAllMethodsServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubPagesServlet.class);

    private static final String REQUEST_PARAMETER_DATA = "data";
    private static final String HIDDEN_PROPERTY = "hidden";

    private static final Set<String> ACCEPTABLE_RESOURCE_TYPES = ImmutableSet.of(NameConstants.NT_PAGE,
            JcrResourceConstants.NT_SLING_FOLDER, JcrResourceConstants.NT_SLING_ORDERED_FOLDER);

    @Override
    protected final void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        String[] roots = request.getParameterValues(REQUEST_PARAMETER_DATA);
        ResourceResolver resourceResolver = request.getResourceResolver();

        List<PageWrap> pageWrapList = new ArrayList<>();
        for (String root : roots) {
            if (StringUtils.isNotEmpty(root)) {
                Resource rootResource = resourceResolver.getResource(root);
                if (rootResource != null) {
                    addPagesTree(pageWrapList, rootResource);
                }
            }
        }

        try {
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(JSONUtil.fromObjectToJsonString(pageWrapList));
        } catch (GCException e) {
            LOGGER.error("Issue during serialisation of list", e);
        }

    }

    private void addPagesTree(List<PageWrap> pageWrapSet, Resource root) {
        Boolean isHidden = root.getValueMap().get(HIDDEN_PROPERTY, Boolean.FALSE);
        if (ACCEPTABLE_RESOURCE_TYPES.contains(root.getResourceType()) && !isHidden) {
            pageWrapSet.add(new PageWrap(root));
        }
        if (!isHidden) {
            Iterator<Resource> children = root.listChildren();
            while (children.hasNext()) {
                addPagesTree(pageWrapSet, children.next());
            }
        }
    }
}
