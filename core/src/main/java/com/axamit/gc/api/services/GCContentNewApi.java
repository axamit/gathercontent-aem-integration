/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;


import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.core.exception.GCException;

import java.util.List;

/**
 * The <tt>GCContentApi</tt> interface provides methods to get information from remote GatherContent server using new API.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCContentNewApi {
    /**
     * Creates a new Item within a particular Project.
     *
     * @param gcItem    Item with Project ID, name, Parent ID, Template ID and Config to create new item.
     * @param gcContext <code>{@link GCContext}</code> object.
     * @return GatherContent ID of created item.
     * @throws GCException If any error occurs during sending information to GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/post-items/">Create an Item</a>
     */
    String createItem(GCItem gcItem, GCContext gcContext) throws GCException;

    /**
     * Saves an Item with the newly updated data.
     *
     * @param gcConfigs Config object containing tabs and field information
     * @param itemId    Item ID.
     * @param gcContext <code>{@link GCContext}</code> object.
     * @return true if item was updated successfully, false otherwise.
     * @throws GCException If any error occurs during sending information to GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/post-items-by-id/">Save an Item</a>
     */
    Boolean updateItem(List<GCConfig> gcConfigs, String itemId, GCContext gcContext) throws GCException;
}
