/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services.impl;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.services.GCConfiguration;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.commons.lang3.math.NumberUtils;
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
     * @return
     */
    @Override
    public Integer getAccountId(final Resource resource) {
        return NumberUtils.toInt(getProperty(resource, PATH_GC_ACCOUNT_ID), 0);
    }

    /**
     * @inheritDoc
     */
    @Override
    public GCContext getGCContext(final Resource resource) {
        // TODO ! Use Sling Models resource mapping and resource.adaptTo(Credentials.class)
        String username = getProperty(resource, PATH_GC_USERNMAME);
        String apikey = getProperty(resource, PATH_GC_APIKEY);
        return GCContext.build(username, apikey);
    }

}
