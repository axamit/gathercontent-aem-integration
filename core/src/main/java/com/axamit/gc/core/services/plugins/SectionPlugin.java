/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins;

import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.core.filters.SystemFieldFilter;
import com.axamit.gc.core.util.GCStringUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Plugin for transformation 'section' GatherContent type.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class SectionPlugin implements GCPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SectionPlugin.class);

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
            throws RepositoryException {
        return SystemFieldFilter.INSTANCE.filter(properties);
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String sectionPropertyPath, final GCElement gcElement,
                                     final Collection<String> updatedProperties, final Map<String, List<Asset>> gcAssets)
            throws RepositoryException {
        Node node = page.adaptTo(Node.class);
        if (node == null || !node.hasProperty(sectionPropertyPath)) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                + "The AEM template has probably been modified after mapping. Please review.", sectionPropertyPath);
            return;
        }
        String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(sectionPropertyPath);
        String property = GCStringUtil.getPropertyNameFromPropertyPath(sectionPropertyPath);
        Node destinationNode = node.getNode(relativePath);
        destinationNode.setProperty(property, gcElement.getSubtitle());
    }

    @Override
    public void transformFromAEMtoGC(final ResourceResolver resourceResolver, final Page page,
                                     final GCElement gcElement, final String propertyPath, final String propertyValue) {
        if (resourceResolver != null && gcElement != null && propertyPath != null && propertyValue != null) {
            Resource resource = resourceResolver.getResource(propertyPath);
            if (resource != null) {
                String value = resource.getValueMap().get(propertyValue, String.class);
                if (value != null) {
                    gcElement.setSubtitle(value);
                }
            }
        }
    }
}
