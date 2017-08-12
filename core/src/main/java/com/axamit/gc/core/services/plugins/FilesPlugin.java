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
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Plugin for transformation 'files' GatherContent type.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class FilesPlugin extends ExportOnlyPlugin {

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
            throws RepositoryException {
        return SystemFieldFilter.INSTANCE.filter(properties);
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String propertyPath, final GCElement gcElement,
                                     final Collection<String> updatedProperties, final Map<String, List<Asset>> gcAssets)
            throws RepositoryException {
        Node node = page.adaptTo(Node.class);
        if (node == null || !node.hasProperty(propertyPath)) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                + "The AEM template has probably been modified after mapping. Please review.", propertyPath);
            return;
        }
        String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
        String property = GCStringUtil.getPropertyNameFromPropertyPath(propertyPath);
        if (gcAssets != null && !gcAssets.isEmpty()) {
            List<Asset> assetList = gcAssets.get(gcElement.getName());
            if (assetList != null && !assetList.isEmpty()) {
                Node destinationNode = node.getNode(relativePath);
                Asset asset = assetList.get(assetList.size() - 1);
                if (asset != null) {
                    destinationNode.setProperty(property, asset.getPath());
                }
            }
        } else {
            Node itemNode = JcrUtils.getNodeIfExists(node, relativePath);
            if (itemNode != null) {
                itemNode.setProperty(property, "");
            }
        }
    }


}
