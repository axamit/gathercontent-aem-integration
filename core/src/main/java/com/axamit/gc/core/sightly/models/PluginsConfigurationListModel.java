/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.google.common.collect.ImmutableList;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import java.util.List;

/**
 * Sling model class which represents table with GC Plugins configuration list on Plugins Configuration page.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = Resource.class)
public final class PluginsConfigurationListModel {

    private final List<PluginsConfigurationModel> pluginsConfigurationList;

    /**
     * Constructor with resource field initialization.
     *
     * @param resource org.apache.sling.api.resource.Resource resource.
     */
    public PluginsConfigurationListModel(final Resource resource) {
        Iterable<Resource> pluginsConfigurationResources = resource.getChildren();
        final ImmutableList.Builder<PluginsConfigurationModel> builder = ImmutableList.builder();
        for (Resource pluginsConfigurationResource : pluginsConfigurationResources) {
            PluginsConfigurationModel pluginsConfigurationModel =
                pluginsConfigurationResource.adaptTo(PluginsConfigurationModel.class);
            builder.add(pluginsConfigurationModel);
        }
        pluginsConfigurationList = builder.build();
    }

    public List<PluginsConfigurationModel> getPluginsConfigurationList() {
        return pluginsConfigurationList;
    }

    @Override
    public String toString() {
        return "PluginsConfigurationListModel{"
            + "pluginsConfigurationList=" + pluginsConfigurationList
            + '}';
    }

    public boolean isEmpty() {
        return pluginsConfigurationList.isEmpty();
    }
}
