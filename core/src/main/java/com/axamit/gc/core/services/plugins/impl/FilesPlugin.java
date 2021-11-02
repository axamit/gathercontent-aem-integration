/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins.impl;

import com.axamit.gc.api.dto.GCContent;
import com.axamit.gc.api.dto.GCTemplateField;
import com.axamit.gc.core.filters.FilesFieldFilter;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.axamit.gc.core.util.GCStringUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.commons.PropertiesUtil;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Plugin for transformation 'files' GatherContent type.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class FilesPlugin extends ImportOnlyPlugin {

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
            throws RepositoryException {
        return FilesFieldFilter.INSTANCE.filter(properties);
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String propertyPath, final GCContent gcContent,
                                     final GCTemplateField gcTemplateField, final Collection<String> updatedProperties, final Map<String, Asset> gcAssets)
            throws RepositoryException {
        final Node node = page.adaptTo(Node.class);
        if (node == null || !node.hasProperty(propertyPath)) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                    + "The AEM template has probably been modified after mapping. Please review.", propertyPath);
            return;
        }
        final String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
        final String property = GCStringUtil.getPropertyNameFromPropertyPath(propertyPath);
        if (gcAssets != null && !gcAssets.isEmpty()) {
            final List<String> paths = new ArrayList<>();
            gcContent.getFiles().forEach(gcFile -> {
                final Asset asset = gcAssets.get(gcFile.getFileId());
                if (asset != null) {
                    paths.add(asset.getPath());
                }
            });
            final Node destinationNode = node.getNode(relativePath);
            if (!paths.isEmpty()) {
                if (updatedProperties.contains(propertyPath)) {
                    //handle case when two GatherContent file fields may be mapped to one field
                    final String[] prevValues = PropertiesUtil.toStringArray(destinationNode.getProperty(property).getValues());
                    destinationNode.setProperty(property, ArrayUtils.addAll(prevValues, paths.toArray(new String[0])));
                } else {
                    destinationNode.setProperty(property, paths.toArray(new String[0]));
                }
                updatedProperties.add(propertyPath);
            }
        } else {
            final Node itemNode = JcrUtils.getNodeIfExists(node, relativePath);
            if (itemNode != null) {
                itemNode.setProperty(property, "");
            }
        }
    }
}
