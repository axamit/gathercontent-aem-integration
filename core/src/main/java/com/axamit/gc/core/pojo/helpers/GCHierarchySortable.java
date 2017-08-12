/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo.helpers;

/**
 * Interface mark classes eligible for naming according to GatherContent hierarchy.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCHierarchySortable {
    /**
     * @return GatherContent Item ID.
     */
    String getId();

    /**
     * @return GatherContent Item ID of parent item.
     */
    String getParentId();
}
