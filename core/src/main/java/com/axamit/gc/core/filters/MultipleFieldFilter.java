/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of <code>{@link FieldFilter}</code> interface which provides methods to filter JCR properties
 * which are suitable for mapping to GatherContent 'Checkboxes' and 'Multiple choice' field - in AEM it is 'options'
 * property of 'Checkbox Group' and 'Radio Group' components of 'Form' component group.
 */
public final class MultipleFieldFilter implements FieldFilter {
    private static final String OPTIONS_PROPERTY_NAME = "options";
    private static final String SLING_RESOURCE_TYPE_PROPERTY_NAME = "sling:resourceType";
    private static final List<String> MULTIPLE_CHOISES_SLING_RESOURCE_TYPES = new ArrayList<>(
            Arrays.asList("foundation/components/form/checkbox", "foundation/components/form/radio"));

    /**
     * @inheritDoc
     */
    @Override
    public List<Property> filter(final List<Property> properties) throws RepositoryException {
        List<Property> filteredProperties = new ArrayList<>();
        for (Property property : properties) {
            if (property.isMultiple()
                    || OPTIONS_PROPERTY_NAME.equals(property.getName())
                    && property.getParent().hasProperty(SLING_RESOURCE_TYPE_PROPERTY_NAME)
                    && MULTIPLE_CHOISES_SLING_RESOURCE_TYPES.contains(
                    property.getParent().getProperty(SLING_RESOURCE_TYPE_PROPERTY_NAME).getString())) {
                filteredProperties.add(property);
            }
        }
        return filteredProperties;
    }
}
