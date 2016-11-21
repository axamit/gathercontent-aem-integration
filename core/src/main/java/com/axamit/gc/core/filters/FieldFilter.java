/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.filters;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.List;

/**
 * The <tt>FieldFilter</tt> interface provides methods to filter list of JCR properties.
 * @author Axamit, gc.support@axamit.com
 */
public interface FieldFilter {
    /**
     * Perform filtering of JCR properties list.
     *
     * @param properties <tt>List</tt> of JCR properties to filter.
     * @return <tt>List</tt> of filtered JCR properties.
     * @throws RepositoryException If any error related to access to JCR repository occurs.
     */
    List<Property> filter(List<Property> properties) throws RepositoryException;
}
