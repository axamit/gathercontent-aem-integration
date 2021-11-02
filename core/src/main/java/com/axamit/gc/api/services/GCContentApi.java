/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;


import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.*;
import com.axamit.gc.core.exception.GCException;
import org.apache.sling.commons.json.JSONObject;

import java.util.List;

/**
 * The <tt>GCContentApi</tt> interface provides methods to get information from remote GatherContent server.
 *
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
    List<GCProject> projects(GCContext gcContext, Integer accountId) throws GCException;

    /**
     * Get list of GatherContent Live statuses information for by project ID.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param projectId Project ID.
     * @return list of GatherContent Live statuses.
     * @throws GCException If any error occurs during requesting information from GatherContent.
     * @see <a href="https://gathercontent.com/developers/projects/get-projects-statuses/">Get all Statuses</a>
     */
    List<GCStatus> statusesByProjectId(GCContext gcContext, Integer projectId) throws GCException;

    /**
     * Perform update of item Live status.
     *
     * @param gcContext <code>{@link GCContext}</code> object.
     * @param itemId    Item ID.
     * @param statusId  Status ID.
     * @return true if item status was updated successfully, false otherwise.
     */
    Boolean updateItemStatus(GCContext gcContext, Integer itemId, Integer statusId);

}
