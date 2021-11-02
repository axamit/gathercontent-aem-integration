/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.impl;

import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.core.services.FailSafeExecutor;
import com.axamit.gc.core.services.plugins.GCPluginManager;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.axamit.gc.core.sightly.models.PluginsConfigurationModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCStringUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OSGI service implements <code>{@link GCPluginManager}</code> interface for obtaining GC Plugins.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPluginManager.class)
@Component(description = "GC Plugin Manager Service", name = "GC Plugin Manager", immediate = true, metatype = true)
@Reference(name = "gCPlugin", referenceInterface = GCPlugin.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        bind = "bindGCPlugin", unbind = "unbindGCPlugin", policy = ReferencePolicy.DYNAMIC)
public final class GCPluginManagerImpl implements GCPluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCPluginManagerImpl.class);
    private static final Map<String, Object> DEFAULT_PLUGINS_CONFIG_PROPERTIES = ImmutableMap.<String, Object>builder()
            .put(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME, Constants.PLUGIN_CONFIG_RESOURCETYPE)
            .put(Constants.PLUGIN_CONFIG_NAME_PN, "Default config")
            .put(GCElementType.FILES.getValue(),
                    "{\"name\":\"\",\"plugin\":\"com.axamit.gc.core.services.plugins.impl.FilesPlugin\"}")
            .put(GCElementType.TEXT.getValue(),
                    new String[]{"{\"name\":\"\",\"plugin\":\"com.axamit.gc.core.services.plugins.impl.TextPlugin\"}"})
            .put(GCElementType.CHOICE_CHECKBOX.getValue(),
                    "{\"name\":\"\",\"plugin\":\"com.axamit.gc.core.services.plugins.impl.OptionsPlugin\"}")
            .put(GCElementType.CHOICE_RADIO.getValue(),
                    "{\"name\":\"\",\"plugin\":\"com.axamit.gc.core.services.plugins.impl.OptionsPlugin\"}")
            .put(GCElementType.OPTIONS.getValue(),
                    "{\"name\":\"\",\"plugin\":\"com.axamit.gc.core.services.plugins.impl.OptionsPlugin\"}")
            .build();
    private static final String SERVICE_PID_PN = "service.pid";
    private final Map<String, GCPlugin> gcPlugins = new HashMap<>();
    @Reference
    private FailSafeExecutor failSafeExecutor;

    /**
     * Method called when the <code>{@link GCPlugin}</code> service is to be bound to the component.
     *
     * @param gcPlugin   <code>{@link GCPlugin}</code> service reference.
     * @param properties <code>{@link GCPlugin}</code> service properties.
     */
    protected void bindGCPlugin(final GCPlugin gcPlugin, final Map<String, Object> properties) {
        synchronized (gcPlugins) {
            String key = (String) properties.get(SERVICE_PID_PN);
            gcPlugins.put(key, gcPlugin);
        }
    }

    /**
     * Method called when the <code>{@link GCPlugin}</code> service is to be unbound from the component.
     *
     * @param gcPlugin   <code>{@link GCPlugin}</code> service reference.
     * @param properties <code>{@link GCPlugin}</code> service properties.
     */
    protected void unbindGCPlugin(final GCPlugin gcPlugin, final Map<String, Object> properties) {
        synchronized (gcPlugins) {
            String key = (String) properties.get(SERVICE_PID_PN);
            if (key != null && !key.isEmpty() && gcPlugins.containsKey(key)) {
                gcPlugins.remove(key);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public GCPlugin getPlugin(final ResourceResolver resourceResolver, final String configurationPath,
                              final String elementType, final String fieldName, final String fieldPlugin) {
        if (StringUtils.isNotEmpty(fieldPlugin) && gcPlugins.containsKey(fieldPlugin)) {
            return gcPlugins.get(fieldPlugin);
        }
        Resource configurationResource = resourceResolver.getResource(configurationPath);
        if (configurationResource == null) {
            LOGGER.error("Configuration resource is null");
            return null;
        }
        PluginsConfigurationModel pluginsConfigurationModel =
                configurationResource.adaptTo(PluginsConfigurationModel.class);
        Map<String, Map<String, String>> pluginMap = pluginsConfigurationModel.getPluginsMap();
        if (pluginMap == null) {
            LOGGER.error("Plugin map is null");
            return null;
        }
        Map<String, String> pluginsForElementType = pluginMap.get(elementType);
        if (pluginsForElementType == null) {
            LOGGER.error("Can't get plugin by element type \"{}\"", elementType);
            return null;
        }
        return pluginsForElementType.get(fieldName) != null
                ? gcPlugins.get(pluginsForElementType.get(fieldName))
                : gcPlugins.get(pluginsForElementType.get(StringUtils.EMPTY));
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<String> getRegisteredPluginsPIDs() {
        return ImmutableList.copyOf(gcPlugins.keySet());
    }

    /**
     * @inheritDoc
     */
    @Override
    public synchronized Resource getOrCreateDefaultPluginsConfig(final ResourceResolver resourceResolver,
                                                                 final Resource cloudServiceResource) {
        try {
            return failSafeExecutor.executeWithRetries(() -> {
                Resource configListResource = resourceResolver.getResource(GCStringUtil
                        .appendNewLevelToPath(cloudServiceResource.getPath(), Constants.PLUGINS_CONFIG_LIST_NN));

                if (configListResource == null) {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME,
                            Constants.PLUGINS_CONFIG_LIST_RESOURCETYPE);
                    configListResource = resourceResolver.create(cloudServiceResource,
                            Constants.PLUGINS_CONFIG_LIST_NN, properties);
                    resourceResolver.commit();
                }
                if (configListResource == null) {
                    LOGGER.error("ConfigListResource is null");
                    return null;
                }
                final Resource defaultPluginConfig =
                        configListResource.getChild(Constants.DEFAULT_PLUGIN_CONFIG_NN);
                if (defaultPluginConfig != null) {
                    return defaultPluginConfig;
                }
                try {
                    return resourceResolver.create(configListResource,
                            Constants.DEFAULT_PLUGIN_CONFIG_NN, DEFAULT_PLUGINS_CONFIG_PROPERTIES);
                } finally {
                    resourceResolver.commit();
                }
            });
        } catch (Exception e) { //! Too broad? Rethrow GCException?
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
