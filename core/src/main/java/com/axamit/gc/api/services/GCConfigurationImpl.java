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
import org.apache.sling.api.resource.ValueMap;

/**
 * OSGI service implements <tt>GCContentApi</tt> interface provides methods to get information about cloudservice
 * instance context config.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCConfiguration.class)
@Component(description = "GatherContent Configuration", name = "GatherContent Configuration",
        immediate = true, metatype = true)
public final class GCConfigurationImpl implements GCConfiguration {

    private static final String PATH_GC_APIKEY = "gcApikey";
    private static final String PATH_GC_USERNMAME = "gcUsername";
    private static final String PATH_GC_IS_NEW_EDITOR = "isNewEditor";
    private static final String PATH_GC_ACCOUNT_ID = "accountId";

    private static String getProperty(final Resource resource, final String path) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(resource);
        final ValueMap properties = page.getProperties();
        return properties.get(path, String.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getAccountId(final Resource resource) {
        return getProperty(resource, PATH_GC_ACCOUNT_ID);
    }

    /**
     * @inheritDoc
     */
    @Override
    public GCContext getGCContext(final Resource resource) {
        //! Use Sling Models resource mapping and resource.adaptTo(Credentials.class)
        String username = getProperty(resource, PATH_GC_USERNMAME);
        String apikey = getProperty(resource, PATH_GC_APIKEY);
        String isNewEditor = getProperty(resource, PATH_GC_IS_NEW_EDITOR);
        return GCContext.build(username, apikey, Boolean.valueOf(isNewEditor));
    }

}
