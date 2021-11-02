/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;


import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.*;
import com.axamit.gc.core.exception.GCException;

import java.util.List;

/**
 * The <tt>GCContentApi</tt> interface provides methods to get information from remote GatherContent server using new API.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface GCContentNewApi {


    /**
     * Get list of GatherContent templates information of a project.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param projectId Project ID.
     * @return list of GatherContent templates.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/templates/get-templates/">Get all Templates</a>
     */
    List<GCTemplateData> templates(GCContext gcContext, Integer projectId) throws GCException;

    /**
     * Get a particular GatherContent template information by project ID and template ID.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param templateId Template ID.
     * @return GatherContent template.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/templates/get-templates-by-id/">Get a Template</a>
     */
    GCTemplate template(GCContext gcContext, Integer templateId) throws GCException;

    /**
     * Get a particular GatherContent item information by item ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param itemId    Item ID.
     * @return GatherContent template.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/get-items-by-id/">Get a single Item</a>
     */
    GCItem itemById(GCContext gcContext, Integer itemId) throws GCException;

    /**
     * Get list of GatherContent items information by project ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param projectId Project ID.
     * @return list of GatherContent items.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/get-items/">Get all Items</a>
     */
    List<GCItem> itemsByProjectId(GCContext gcContext, Integer projectId) throws GCException;

    /**
     * Creates a new Item within a particular Project.
     *
     * @param gcItem    Item with Project ID, name, Parent ID, Template ID and Config to create new item.
     * @param gcContext <code>{@link GCContext}</code> object.
     * @return GatherContent ID of created item.
     * @throws GCException If any error occurs during sending information to GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/post-items/">Create an Item</a>
     */
    Integer createItem(GCItem gcItem, GCContext gcContext) throws GCException;

    /**
     * Saves an Item with the newly updated data.
     *
     * @param gcItem    Item with Project ID, name, Parent ID, Template ID and Config to update item.
     * @param gcContext <code>{@link GCContext}</code> object.
     * @return true if item was updated successfully, false otherwise.
     * @throws GCException If any error occurs during sending information to GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/post-items-by-id/">Save an Item</a>
     */
    Boolean updateItemContent(GCItem gcItem, GCContext gcContext) throws GCException;

    List<GCFolder> foldersByProjectId(GCContext gcContext, Integer projectId) throws GCException;
}
