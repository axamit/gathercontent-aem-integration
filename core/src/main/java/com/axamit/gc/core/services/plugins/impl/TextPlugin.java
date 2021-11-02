/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins.impl;

import com.axamit.gc.api.dto.GCContent;
import com.axamit.gc.api.dto.GCTemplateField;
import com.axamit.gc.core.filters.SystemFieldFilter;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.axamit.gc.core.util.GCStringUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Map;

/**
 * Plugin for transformation 'text' GatherContent type into AEM text field.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class TextPlugin implements GCPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextPlugin.class);

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
            throws RepositoryException {
        return SystemFieldFilter.INSTANCE.filter(properties);
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String textPropertyPath, final GCContent gcContent,
                                     final GCTemplateField gcTemplateField, final Collection<String> updatedProperties, final Map<String, Asset> gcAssets)
            throws RepositoryException {
        Node node = page.adaptTo(Node.class);
        if (node == null || !node.hasProperty(textPropertyPath)) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                    + "The AEM template has probably been modified after mapping. Please review.", textPropertyPath);
            return;
        }
        String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(textPropertyPath);
        String property = GCStringUtil.getPropertyNameFromPropertyPath(textPropertyPath);
        Node destinationNode = node.getNode(relativePath);
        // concatenation of multiple GatherContent text fields into a single AEM text field
        if (updatedProperties.contains(textPropertyPath)) {
            destinationNode.setProperty(property, destinationNode.getProperty(property).getString()
                    + gcContent.getText());
        } else {
            destinationNode.setProperty(property, String.valueOf(gcContent.getText()));
        }
        updatedProperties.add(textPropertyPath);
    }

    @Override
    public void transformFromAEMtoGC(final ResourceResolver resourceResolver, final Page page,
                                     final GCContent gcContent, final String propertyPath, final String propertyValue) {
        if (resourceResolver != null && propertyPath != null && propertyValue != null) {
            Resource resource = resourceResolver.getResource(propertyPath);
            if (resource != null) {
                String value = resource.getValueMap().get(propertyValue, String.class);
                if (value != null) {
                    gcContent.setText(value);
                }
            }
        }
    }
}
