/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.core.services.plugins.GCPlugin;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;

/**
 * The <tt>GCPluginManager</tt> interface provides methods for obtaining GC Plugins.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCPluginManager {
    /**
     * Method for obtaining suitable <code>{@link GCPlugin}</code>.
     *
     * @param resourceResolver  JCR ResourceResolver.
     * @param configurationPath JCR path to GC Plugins configuration.
     * @param elementType       GatherContent field type.
     * @param fieldName         JCR property name in AEM for more fine-tuning of field mapping.
     * @param fieldPluginPID    Plugin PID set for this field.
     * @return <code>{@link GCPlugin}</code> suitable for this field type.
     */
    GCPlugin getPlugin(ResourceResolver resourceResolver, String configurationPath,
                       String elementType, String fieldName, String fieldPluginPID);

    /**
     * @return <code>List</code> of all registered <code>{@link GCPlugin}</code> services PIDs in AEM.
     */
    List<String> getRegisteredPluginsPIDs();

    /**
     * Get default GC Plugins configuration resource of this GatherContent cloud service configuration if exists or
     * create and then return if it does not exists.
     *
     * @param resourceResolver     JCR ResourceResolver.
     * @param cloudServiceResource Resource of current cloudservice configuration.
     * @return Default GC Plugins configuration resource.
     */
    Resource getOrCreateDefaultPluginsConfig(ResourceResolver resourceResolver, Resource cloudServiceResource);
}
