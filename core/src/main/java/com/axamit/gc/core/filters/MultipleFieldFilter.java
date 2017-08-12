/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import com.axamit.gc.core.util.Constants;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Implementation of <code>{@link FieldFilter}</code> interface which provides methods to filter JCR properties
 * which are suitable for mapping to GatherContent 'Checkboxes' and 'Multiple choice' field - in AEM it is 'options'
 * property of 'Checkbox Group' and 'Radio Group' components of 'Form' component group.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum MultipleFieldFilter implements FieldFilter {
    INSTANCE;

    private static final String OPTIONS_PROPERTY_NAME = "options";
    private static final List<String> MULTIPLE_CHOICES_SLING_RESOURCE_TYPES =
        ImmutableList.of("foundation/components/form/checkbox", "foundation/components/form/radio");

    /**
     * @inheritDoc
     */
    @Override
    public Collection<Property> filter(final Collection<Property> properties) throws RepositoryException {
        final ImmutableList.Builder<Property> filteredProperties = ImmutableList.builder();
        for (Property property : properties) {
            if (OPTIONS_PROPERTY_NAME.equals(property.getName())
                && property.getParent().hasProperty(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME)
                && MULTIPLE_CHOICES_SLING_RESOURCE_TYPES.contains(
                property.getParent().getProperty(Constants.SLING_RESOURCE_TYPE_PROPERTY_NAME).getString())) {
                filteredProperties.add(property);
            }
        }
        return filteredProperties.build();
    }
}
