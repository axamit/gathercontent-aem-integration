/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class represents cloudservice context config.
 * @author Axamit, gc.support@axamit.com
 */
public final class GCContext implements Serializable {

    private String username;
    private String apikey;
    private Map<String, String> headers;
    private String apiURL = "https://api.gathercontent.com/";

    private GCContext(final String username, final String apikey) {
        this.username = username;
        this.apikey = apikey;
        headers = new HashMap<>();
        headers.put("Accept", "application/vnd.gathercontent.v0.5+json");
    }

    /**
     * Build <code>{@link GCContext}</code> object.
     *
     * @param username GatherContent Username.
     * @param apikey   GatherContent Api Key,
     * @return Created <code>{@link GCContext}</code> object.
     */
    public static GCContext build(final String username, final String apikey) {
        return new GCContext(username, apikey);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(final String apikey) {
        this.apikey = apikey;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getApiURL() {
        return apiURL;
    }
}
