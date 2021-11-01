/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.impl;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.services.FailSafeExecutor;
import com.axamit.gc.core.services.ImportService;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.ResourceResolverUtil;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.concurrent.Callable;

/**
 * OSGI service implementS <tt>ImportService</tt> interface provides methods to get and update status of specific
 * import/update process job.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = ImportService.class)
@Component(description = "Import Service Service", name = "Import Service", immediate = true, metatype = true)
public final class ImportServiceImpl implements ImportService {

    private static final String GATHERCONTENT_COMPONENTS_CONTENT_IMPORT = "gathercontent/components/content/import";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    @Reference
    private FailSafeExecutor failSafeExecutor;

    private static Resource getOrCreateResource(final ResourceResolver resourceResolver, final String pagePath,
                                                final String jobId, final boolean createIfNull)
        throws PersistenceException, RepositoryException {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getPage(pagePath);
        if (page == null) {
            //! Log
            return null;
        }
        Resource importListResource = page.getContentResource().getChild(Constants.IMPORT_LIST_NN);
        if (importListResource == null) {
            //! Log
            return null;
        }
        Resource importNode = importListResource.getChild(jobId);
        if (importNode != null || !createIfNull) {
            return importNode;
        }
        Session session = resourceResolver.adaptTo(Session.class);
        Node itemNodeContent = JcrUtil.createPath(importListResource.adaptTo(Node.class), jobId, false,
            JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, session, false);
        itemNodeContent.setProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
            GATHERCONTENT_COMPONENTS_CONTENT_IMPORT);
        resourceResolver.commit();

        return resourceResolver.getResource(itemNodeContent.getPath());
    }

    @Override
    public void updateStatus(final String pagePath, final ImportModel importModel) throws GCException {
        try {
            failSafeExecutor.executeWithRetries((Callable<Void>) () -> {
                ResourceResolver resourceResolver = null;
                try {
                    resourceResolver = getDefaultServiceResourceResolver();
                    Resource resource =
                        getOrCreateResource(resourceResolver, pagePath, importModel.getJobId(), true);
                    if (resource == null) {
                        //! Log
                        return null;
                    }
                    Node node = resource.adaptTo(Node.class);

                    node.setProperty(ImportModel.PROPERTY_JOB_ID, importModel.getJobId());
                    node.setProperty(ImportModel.PROPERTY_IMPORT_ID, importModel.getImportId());
                    node.setProperty(ImportModel.PROPERTY_IMPORTED_PAGES_DATA,
                        importModel.getImportedPagesData());
                    node.setProperty(ImportModel.PROPERTY_IMPORTED_PAGES_COUNT,
                        importModel.getImportedPagesCount());
                    node.setProperty(ImportModel.PROPERTY_TOTAL_PAGES_COUNT, importModel.getTotalPagesCount());
                    node.setProperty(ImportModel.PROPERTY_IMPORT_START_DATE, importModel.getImportStartDate());
                    node.setProperty(ImportModel.PROPERTY_IMPORT_END_DATE, importModel.getImportEndDate());
                    node.setProperty(ImportModel.PROPERTY_PROJECT_NAME, importModel.getProjectName());
                    node.setProperty(ImportModel.PROPERTY_JOB_TYPE, importModel.getJobType());
                    node.setProperty(ImportModel.PROPERTY_STATUS, importModel.getStatus());
                    node.getSession().save();
                } finally {
                    if (resourceResolver != null && resourceResolver.isLive()) {
                        resourceResolver.close();
                    }
                }
                return null;
            });
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new GCException(ex);
        }
    }

    @Override
    public ImportModel getImportStatus(final String pagePath, final String jobId) throws GCException {
        try {
            return failSafeExecutor.executeWithRetries(() -> {
                ResourceResolver resourceResolver = null;
                try {
                    resourceResolver = getDefaultServiceResourceResolver();
                    Resource resource = getOrCreateResource(resourceResolver, pagePath, jobId, false);
                    if (resource != null) {
                        return resource.adaptTo(ImportModel.class);
                    }
                } finally {
                    if (resourceResolver != null && resourceResolver.isLive()) {
                        resourceResolver.close();
                    }
                }
                return null;
            });
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new GCException(ex);
        }
    }

    private ResourceResolver getDefaultServiceResourceResolver() throws LoginException {
        return ResourceResolverUtil.getResourceResolver(resourceResolverFactory, "PageCreator");
    }
}
