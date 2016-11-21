/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;


import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCData;
import com.axamit.gc.api.dto.GCFile;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.core.exception.GCException;
import org.apache.sling.commons.json.JSONObject;

import java.util.List;

/**
 * The <tt>GCContentApi</tt> interface provides methods to get information from remote GatherContent server.
 * @author Axamit, gc.support@axamit.com
 */
public interface GCContentApi {
    /**
     * Get JSONObject representation of GatherContent user information.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @return JSONObject with user information.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/me/get-me/">User information</a>
     */
    JSONObject me(GCContext gcContext) throws GCException;

    /**
     * Get list of GatherContent accounts information.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @return list of GatherContent accounts.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/accounts/get-accounts/">Get all Accounts</a>
     */
    List<GCAccount> accounts(GCContext gcContext) throws GCException;

    /**
     * Get list of GatherContent projects information on an account.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param accountId Account ID of user.
     * @return list of GatherContent accounts.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/projects/get-projects/">Get all Projects</a>
     */
    List<GCProject> projects(GCContext gcContext, String accountId) throws GCException;

    /**
     * Get list of GatherContent templates information of a project.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param projectId Project ID.
     * @return list of GatherContent templates.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/templates/get-templates/">Get all Templates</a>
     */
    List<GCTemplate> templates(GCContext gcContext, String projectId) throws GCException;

    /**
     * Get a particular GatherContent template information by project ID and template ID.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param projectId  Project ID.
     * @param templateId Template ID.
     * @return GatherContent template.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/templates/get-templates-by-id/">Get a Template</a>
     */
    GCTemplate template(GCContext gcContext, String projectId, String templateId) throws GCException;

    /**
     * Get a particular GatherContent item information by item ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param itemId    Item ID.
     * @return GatherContent template.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/get-items-by-id/">Get a single Item</a>
     */
    GCItem itemById(GCContext gcContext, String itemId) throws GCException;

    /**
     * Get list of GatherContent items information by project ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param projectId Project ID.
     * @return list of GatherContent items.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/get-items/">Get all Items</a>
     */
    List<GCItem> itemsByProjectId(GCContext gcContext, String projectId) throws GCException;

    /**
     * Get list of GatherContent items information by project ID and template ID.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param projectId  Project ID.
     * @param templateId Template ID.
     * @param fetch      A boolean governing fetching of all information about item.
     * @return list of GatherContent items.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/get-items/">Get all Items</a>
     */
    List<GCItem> itemsByProjectIdandTemplateId(GCContext gcContext, String projectId, String templateId,
                                               boolean fetch) throws GCException;

    /**
     * Get list of GatherContent Live statuses information for by project ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param projectId Project ID.
     * @return list of GatherContent Live statuses.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/projects/get-projects-statuses/">Get all Statuses</a>
     */
    List<GCData> statusesByProjectId(GCContext gcContext, String projectId) throws GCException;

    /**
     * Get list of GatherContent files by item ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param itemId    Item ID.
     * @return list of GatherContent files.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/get-items-files/">Get a list of Files</a>
     */
    List<GCFile> filesByItemId(GCContext gcContext, String itemId) throws GCException;

    /**
     * Perform update of item Live status.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param itemId    Item ID.
     * @param statusId  Status ID.
     * @return true if update was performed successfully.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/items/post-items-choose_status/">Choose a Status</a>
     */
    Boolean updateItemStatus(GCContext gcContext, String itemId, String statusId) throws GCException;
}
