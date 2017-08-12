/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins;

import com.axamit.gc.api.dto.GCElement;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin supports only export from AEM to GC.
 *
 * @author Axamit, gc.support@axamit.com
 */
public abstract class ExportOnlyPlugin implements GCPlugin {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ExportOnlyPlugin.class);

    @Override
    public final void transformFromAEMtoGC(final ResourceResolver resourceResolver, final Page page,
                                           final GCElement gcElement, final String propertyPath,
                                           final String propertyValue) {
        LOGGER.info("Transformation from AEM to GC is not implemented for {}", propertyPath);
    }
}
