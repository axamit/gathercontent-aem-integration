/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import com.day.cq.wcm.api.NameConstants;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of <code>{@link FieldFilter}</code> interface which provides methods to filter JCR properties to
 * exclude AEM 'system' properties which are not suitable for mapping like all string with 'cq:' or 'sling:' prefixes.
 */
public final class SystemFieldFilter implements FieldFilter {

    public static final String CQ_PREFIX = "cq:";
    public static final String SLING_PREFIX = "sling:";
    private static List<String> forbiddenProperties = new ArrayList<>();

    static {
        forbiddenProperties.add(NameConstants.PN_CREATED);
        forbiddenProperties.add(NameConstants.PN_CREATED_BY);
        forbiddenProperties.add(NameConstants.PN_LAST_MOD);
        forbiddenProperties.add(NameConstants.PN_LAST_MOD_BY);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Property> filter(final List<Property> properties) throws RepositoryException {
        List<Property> filteredProperties = new ArrayList<>();
        for (Property property : properties) {
            if (!forbiddenProperties.contains(property.getName()) && !property.getName().startsWith(CQ_PREFIX)
                    && !property.getName().startsWith(SLING_PREFIX) && !property.getDefinition().isProtected()) {
                filteredProperties.add(property);
            }
        }
        return filteredProperties;
    }
}
