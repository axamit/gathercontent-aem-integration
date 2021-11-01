/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import com.axamit.gc.core.util.Constants;
import com.google.common.collect.ImmutableList;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of <code>{@link FieldFilter}</code> interface which provides methods to filter JCR properties
 * which are suitable for mapping to GatherContent 'Files' field - in AEM it is Files Component of 'GatherContent Components' component group.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum FilesFieldFilter implements FieldFilter {
    INSTANCE;

    private static final String PATHS_PROPERTY_NAME = "paths";
    private static final List<String> FILES_SLING_RESOURCE_TYPES =
        ImmutableList.of("gathercontent/components/content/files-component");

    /**
     * @inheritDoc
     */
    @Override
    public Collection<Property> filter(final Collection<Property> properties) throws RepositoryException {
        final ImmutableList.Builder<Property> filteredProperties = ImmutableList.builder();
        for (Property property : properties) {
            if (PATHS_PROPERTY_NAME.equals(property.getName())
                && property.getParent().hasProperty(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME)
                && FILES_SLING_RESOURCE_TYPES.contains(
                property.getParent().getProperty(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME).getString())) {
                filteredProperties.add(property);
            }
        }
        return filteredProperties.build();
    }
}
