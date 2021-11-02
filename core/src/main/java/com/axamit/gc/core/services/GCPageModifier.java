/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;

import java.util.List;

/**
 * The <tt>GCPageModifier</tt> interface provides methods to create items in GatherContent.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCPageModifier {
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
    List<ImportResultItem> createPage(List<ImportItem> importItemsToMerge, GCContext gcContext,
                                      List<ImportItem> childrenItems, Integer projectId);

    /**
     * Update item (page) in GatherContent.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param importItem <code>{@link ImportItem}</code> object with information about page need to be updated.
     * @return <code>{@link ImportResultItem}</code> with information about updated item (page).
     */
    ImportResultItem updatePage(GCContext gcContext, ImportItem importItem);
}

