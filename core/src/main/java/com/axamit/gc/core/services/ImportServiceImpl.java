/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.ResourceResolverUtil;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * OSGI service implementS <tt>ImportService</tt> interface provides methods to get and update status of specific
 * import/update process job.
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = ImportService.class)
@Component(description = "Import Service Service", name = "Import Service", immediate = true, metatype = true)
public final class ImportServiceImpl implements ImportService {

    private static final String IMPORT_LIST = "import-list";
    private static final String GATHERCONTENT_COMPONENTS_CONTENT_IMPORT = "gathercontent/components/content/import";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportServiceImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private static Resource getOrCreateResource(final ResourceResolver resourceResolver, final String pagePath,
                                                final String jobId, final boolean createIfNull)
            throws PersistenceException, RepositoryException {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getPage(pagePath);
        if (page != null) {
            Resource importListResource = page.getContentResource().getChild(IMPORT_LIST);
            if (importListResource != null) {
                Resource importNode = importListResource.getChild(jobId);
                if (importNode != null || !createIfNull) {
                    return importNode;
                } else {
                    Session session = resourceResolver.adaptTo(Session.class);
                    Node itemNodeContent = JcrUtil.createPath(importListResource.adaptTo(Node.class), jobId, false,
                            JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, session, false);
                    itemNodeContent.setProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
                            GATHERCONTENT_COMPONENTS_CONTENT_IMPORT);
                    resourceResolver.commit();
                    return resourceResolver.getResource(itemNodeContent.getPath());
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void updateStatus(final String pagePath, final ImportModel importModel) throws GCException {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getDefaultServiceResourceResolver();
            Resource resource = getOrCreateResource(resourceResolver, pagePath, importModel.getJobId(), true);
            if (resource != null) {
                Node node = resource.adaptTo(Node.class);

                node.setProperty(ImportModel.PROPERTY_JOB_ID, importModel.getJobId());
                node.setProperty(ImportModel.PROPERTY_IMPORTED_PAGES_DATA, importModel.getImportedPagesData());
                node.setProperty(ImportModel.PROPERTY_IMPORTED_PAGES_COUNT, importModel.getImportedPagesCount());
                node.setProperty(ImportModel.PROPERTY_TOTAL_PAGES_COUNT, importModel.getTotalPagesCount());
                node.getSession().save();
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new GCException(ex);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

    @Override
    public ImportModel getImportStatus(final String pagePath, final String jobId) throws GCException {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getDefaultServiceResourceResolver();
            Resource resource = getOrCreateResource(resourceResolver, pagePath, jobId, false);
            if (resource != null) {
                return resource.adaptTo(ImportModel.class);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new GCException(ex);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        return null;
    }

    private ResourceResolver getDefaultServiceResourceResolver() throws LoginException {
        return ResourceResolverUtil.getResourceResolver(resourceResolverFactory, "PageCreator");
    }
}
