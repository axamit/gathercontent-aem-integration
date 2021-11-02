/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins.impl;

import com.axamit.gc.api.dto.GCContent;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin supports only import from GC to AEM.
 *
 * @author Axamit, gc.support@axamit.com
 */
public abstract class ImportOnlyPlugin implements GCPlugin {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ImportOnlyPlugin.class);

    @Override
    public final void transformFromAEMtoGC(final ResourceResolver resourceResolver, final Page page,
                                           final GCContent gcContent, final String propertyPath,
                                           final String propertyValue) {
        LOGGER.info("Transformation from AEM to GC is not implemented for {}", propertyPath);
    }
}
