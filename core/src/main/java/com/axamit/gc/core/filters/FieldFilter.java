/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Collection;

/**
 * The <tt>FieldFilter</tt> interface provides methods to filter list of JCR properties.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface FieldFilter {
    /**
     * Perform filtering of JCR properties list.
     *
     * @param properties <tt>Collection</tt> of JCR properties to filter.
     * @return <tt>Collection</tt> of filtered JCR properties.
     * @throws RepositoryException If any error related to access to JCR repository occurs.
     */
    Collection<Property> filter(Collection<Property> properties) throws RepositoryException;
}
