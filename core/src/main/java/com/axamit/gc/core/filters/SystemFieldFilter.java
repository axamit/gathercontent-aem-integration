/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import com.day.cq.wcm.api.NameConstants;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Implementation of <code>{@link FieldFilter}</code> interface which provides methods to filter JCR properties to
 * exclude AEM 'system' properties which are not suitable for mapping like all string with 'cq:' or 'sling:' prefixes.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum SystemFieldFilter implements FieldFilter {
    INSTANCE;

    private static final String CQ_PREFIX = "cq:";
    private static final String SLING_PREFIX = "sling:";
    private static final List<String> FORBIDDEN_PROPERTIES = ImmutableList.of(
        NameConstants.PN_CREATED,
        NameConstants.PN_CREATED_BY,
        NameConstants.PN_LAST_MOD,
        NameConstants.PN_LAST_MOD_BY);

    /**
     * @inheritDoc
     */
    @Override
    public Collection<Property> filter(final Collection<Property> properties) throws RepositoryException {
        final ImmutableList.Builder<Property> filteredProperties = ImmutableList.builder();
        for (Property property : properties) {
            if (!FORBIDDEN_PROPERTIES.contains(property.getName()) && !property.getName().startsWith(CQ_PREFIX)
                && !property.getName().startsWith(SLING_PREFIX) && !property.getDefinition().isProtected()) {
                filteredProperties.add(property);
            }
        }
        return filteredProperties.build();
    }
}
