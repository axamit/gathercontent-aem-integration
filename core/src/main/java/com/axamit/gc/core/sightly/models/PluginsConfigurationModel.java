/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Sling model class which represents GC Plugins configuration.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = Resource.class)
public final class PluginsConfigurationModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginsConfigurationModel.class);
    private static final String NAME_PN = "name";
    private static final String PLUGIN_PN = "plugin";
    private final Resource resource;
    private final Map<String, Map<String, String>> pluginsMap;
    @Inject
    @Optional
    private String configurationName;

    /**
     * Constructor with resource field initialization.
     *
     * @param resource org.apache.sling.api.resource.Resource resource.
     */
    public PluginsConfigurationModel(final Resource resource) {
        this.resource = resource;

        Node configNode = resource.adaptTo(Node.class);
        pluginsMap = scanConfig(configNode);
    }

    private static Map<String, Map<String, String>> scanConfig(Node configNode) {
        if (configNode == null) {
            //! Log
            return ImmutableMap.of();
        }
        final ImmutableMap.Builder<String, Map<String, String>> plugins = ImmutableMap.builder();
        for (GCElementType gcElementType : GCElementType.values()) {
            try {
                final String type = gcElementType.getType();
                if (!configNode.hasProperty(type)) {
                    //! Log
                    continue;
                }
                plugins.put(type, scanProperties(configNode, type));
            } catch (RepositoryException | GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return plugins.build();
    }

    private static Map<String, String> scanProperties(Node configNode, String type)
        throws RepositoryException, GCException {
        Property property = configNode.getProperty(type);
        ImmutableMap.Builder<String, String> elementTypeMap = ImmutableMap.builder();
        if (property.isMultiple()) {
            String[] values = PropertiesUtil.toStringArray(property.getValues(), ArrayUtils.EMPTY_STRING_ARRAY);
            for (String value : values) {
                elementTypeMap.put(getEntry(value));
            }
        } else {
            elementTypeMap.put(getEntry(property.getString()));
        }
        return elementTypeMap.build();
    }

    private static Map.Entry<String, String> getEntry(String string) throws GCException {
        final Map<String, String> pluginConfigMap = JSONUtil.fromJsonToMapObject(string, String.class, String.class);
        return new ImmutablePair<>(pluginConfigMap.get(NAME_PN), pluginConfigMap.get(PLUGIN_PN));
    }

    public String getConfigurationName() {
        return configurationName;
    }

    /**
     * @return <code>Map</code>&lt;GC element type, <code>Map</code> &lt;GC field name, Plugin PID&gt;&gt;
     */
    public Map<String, Map<String, String>> getPluginsMap() {
        return pluginsMap;
    }

    public Resource getResource() {
        return resource;
    }

    /**
     * @return Link to plugins configuration edit page.
     */
    public String getCreateEditlink() {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(resource);
        return page.getPath() + ".config.config-" + resource.getName() + ".html";
    }
}
