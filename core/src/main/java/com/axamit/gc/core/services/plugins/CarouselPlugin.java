/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins;

import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.core.filters.SystemFieldFilter;
import com.axamit.gc.core.util.GCStringUtil;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Plugin for transformation 'files' GatherContent type to AEM 'Carousel'-like Components.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class CarouselPlugin extends ExportOnlyPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarouselPlugin.class);
    private static final String ALL_DIGITS_REGEXP = "[0-9]";

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
        if (node == null) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                    + "The AEM template has probably been modified after mapping. Please review.", propertyPath);
            return;
        }
        String itemNodePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
        String parentNodePath = GCStringUtil.getParentPath(itemNodePath);
        Node parentNode = JcrUtils.getNodeIfExists(node, parentNodePath);
        String itemNodeName = GCStringUtil.getPropertyNameFromPropertyPath(itemNodePath);
        if (parentNode != null) {
            boolean isAssetsEmpty = gcAssets == null || gcAssets.isEmpty();
            String name = itemNodeName.replaceAll(ALL_DIGITS_REGEXP, StringUtils.EMPTY);
            NodeIterator nodes = parentNode.getNodes(name + "*");
            while (nodes.hasNext()) {
                Node targetNode = nodes.nextNode();
                targetNode.remove();
            }
            if (isAssetsEmpty) {
                LOGGER.warn("Empty assets in {}", propertyPath);
                return;
            }
            List<Asset> assets = gcAssets.get(gcElement.getName());
            //create new node for new slide for every asset, except destination node
            if (assets != null && !assets.isEmpty()) { //! Else?
                if (JcrUtils.getNodeIfExists(node, itemNodePath) == null) {
                    JcrUtil.createUniqueNode(parentNode, itemNodeName, JcrConstants.NT_UNSTRUCTURED, node.getSession());
                }
                Node destinationNode = node.getNode(itemNodePath);
                String property = GCStringUtil.getPropertyNameFromPropertyPath(propertyPath);
                boolean isDestinationUpdated = false;
                for (Asset asset : assets) {
                    if (asset != null) {  //! Else?
                        if (!isDestinationUpdated) {
                            destinationNode.setProperty(property, asset.getPath());
                            isDestinationUpdated = true;
                        } else {
                            Node copy = JcrUtil.createUniqueNode(parentNode, name,
                                    JcrConstants.NT_UNSTRUCTURED, destinationNode.getSession());
                            PropertyIterator propertyIterator = destinationNode.getProperties();
                            while (propertyIterator.hasNext()) {
                                JcrUtil.copy(propertyIterator.nextProperty(), copy, null);
                            }
                            copy.setProperty(property, asset.getPath());
                        }
                    }
                }
            }
        }
    }
}
