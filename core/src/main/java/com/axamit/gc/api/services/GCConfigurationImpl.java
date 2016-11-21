/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;

import com.axamit.gc.api.GCContext;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * OSGI service implements <tt>GCContentApi</tt> interface provides methods to get information about cloudservice
 * instance context config.
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCConfiguration.class)
@Component(description = "GatherContent Configuration", name = "GatherContent Configuration",
        immediate = true, metatype = true)
public final class GCConfigurationImpl implements GCConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCConfigurationImpl.class);

    public static final String CONFIGURATION = "gc";

    // TODO
    public static final String PATH_GC_APIKEY = "jcr:content/gcApikey";
    public static final String PATH_GC_USERNMAME = "jcr:content/gcUsername";
    public static final String PATH_GC_ACCOUNT_ID = "jcr:content/accountId";

    /*
    @Reference
    ConfigurationManager configurationManager;
    */

    /**
     * @inheritDoc
     */
    @Override
    public GCContext getGCContext(final Resource resource) {
        String username = getProperty(resource, PATH_GC_USERNMAME);
        String apikey = getProperty(resource, PATH_GC_APIKEY);
        return GCContext.build(username, apikey);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getAccountId(final Resource resource) {
        return getProperty(resource, PATH_GC_ACCOUNT_ID);
    }

    private String getProperty(final Resource resource, final String path) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(resource);
        Node pageNode = page.adaptTo(Node.class);
        String result = null;
        try {
            if (pageNode.hasProperty(path)) {
                result = pageNode.getProperty(path).getString();
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }


    //for security reason congigartion manager is disabled by default
    /*public Configuration findConfiguration(Resource resource) {
        HierarchyNodeInheritanceValueMap pageProperties = new HierarchyNodeInheritanceValueMap(resource);
        String[] services = pageProperties.getInherited("cq:cloudserviceconfigs", new String[]{});
        Configuration cfg = configurationManager.getConfiguration(CONFIGURATION, services);
        return cfg;
    }*/
}
