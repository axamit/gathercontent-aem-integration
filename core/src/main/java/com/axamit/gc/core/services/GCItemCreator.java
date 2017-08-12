/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;

import java.util.List;

/**
 * The <tt>GCItemCreator</tt> interface provides methods to create items in GatherContent.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCItemCreator {
    /**
     * Create or update item (page) in GatherContent.
     *
     * @param importItemsToMerge Collection of <code>{@link ImportItem}</code> objects with information about AEM pages
     *                           need to be merged and created in GatherContent as single page.
     * @param gcContext          <code>{@link GCContext}</code> object.
     * @param childrenItems      Collection of <code>{@link ImportItem}</code> objects which are children of
     *                           importItemsToMerge.
     * @param projectId          Id of project
     * @return Collection of <code>{@link ImportResultItem}</code> with information about created item (page).
     */
    List<ImportResultItem> createGCPage(List<ImportItem> importItemsToMerge, GCContext gcContext,
                                        List<ImportItem> childrenItems, String projectId);

    /**
     * Update item (page) in GatherContent.
     *
     * @param importItem <code>{@link ImportItem}</code> object with information about page need to be updated.
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @return <code>{@link ImportResultItem}</code> with information about updated item (page).
     */
    ImportResultItem updateGCPage(ImportItem importItem, GCContext gcContext);
}

