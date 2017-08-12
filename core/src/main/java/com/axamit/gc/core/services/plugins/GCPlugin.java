/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins;

import com.axamit.gc.api.dto.GCElement;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The <tt>GCPlugin</tt> interface provides methods for transformation of content in import process
 * from GatherContent to AEM and vice versa.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCPlugin {
    /**
     * Method to filter JCR properties suitable for Plugin/field type.
     *
     * @param resourceResolver JCR resourceResolver.
     * @param properties       <code>List</code> of JCR properties to filter.
     * @return Filtered <code>List</code> of JCR properties.
     * @throws RepositoryException If any error occurs during access JCR Repository
     */
    Collection<Property> filter(ResourceResolver resourceResolver, Collection<Property> properties) throws RepositoryException;

    /**
     * Method for transformation content from GatherContent format to AEM format.
     *
     * @param resourceResolver  JCR resourceResolver.
     * @param page              WCM Page.
     * @param propertyPath      JCR path to target AEM property.
     * @param gcElement         Source GatherContent element.
     * @param updatedProperties Collection of already updated properties on this page.
     * @param gcAssets          <code>Map</code> of AEM Assets created for page.
     * @throws RepositoryException If any error occurs during access JCR Repository
     */
    void transformFromGCtoAEM(ResourceResolver resourceResolver, Page page, String propertyPath, GCElement gcElement,
                              Collection<String> updatedProperties, Map<String, List<Asset>> gcAssets)
            throws RepositoryException;

    /**
     * Method for transformation content from AEM format to GatherContent format.
     *
     * @param resourceResolver JCR ResourceResolver.
     * @param page             WCM Page.
     * @param gcElement        Target GatherContent element.
     * @param propertyPath     JCR path to AEM property.
     * @param propertyValue    Value of AEM property.
     * @throws RepositoryException If any error occurs during access JCR Repository
     */
    void transformFromAEMtoGC(ResourceResolver resourceResolver, Page page, GCElement gcElement, String propertyPath,
                              String propertyValue) throws RepositoryException;
}
