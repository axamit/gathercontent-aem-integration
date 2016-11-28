/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The <code>GCStatus</code> class represents live status.
 *
 * @see <a href="https://gathercontent.com/developers/projects/get-projects-statuses/">Statuses</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCStatus {

    private GCData data;

    /**
     * @return Data structure of live status.
     */
    public GCData getData() {
        return data;
    }

    public void setData(final GCData data) {
        this.data = data;
    }
}
