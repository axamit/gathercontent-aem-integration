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
 * which are suitable for mapping to GatherContent 'Checkboxes' and 'Multiple choice' field - in AEM it is 'options'
 * property of 'Checkbox Group' and 'Radio Group' components of 'Form' component group.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum OptionsFieldFilter implements FieldFilter {
    INSTANCE;

    private static final String ITEMS_PROPERTY_NAME = "items";
    private static final List<String> MULTIPLE_CHOICES_SLING_RESOURCE_TYPES =
            ImmutableList.of("gathercontent/components/content/options-component");

    /**
     * @inheritDoc
     */
    @Override
    public Collection<Property> filter(final Collection<Property> properties) throws RepositoryException {
        final ImmutableList.Builder<Property> filteredProperties = ImmutableList.builder();
        for (Property property : properties) {
            if (ITEMS_PROPERTY_NAME.equals(property.getName())
                    && property.getParent().getParent().hasProperty(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME)
                    && MULTIPLE_CHOICES_SLING_RESOURCE_TYPES.contains(
                    property.getParent().getParent().getProperty(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME).getString())) {
                filteredProperties.add(property);
            }
        }
        return filteredProperties.build();
    }
}
