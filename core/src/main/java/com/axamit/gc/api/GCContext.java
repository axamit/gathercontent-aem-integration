/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api;

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.Map;

/**
 * Class represents cloudservice context config.
 *
 * @author Axamit, gc.support@axamit.com
 */
public final class GCContext implements Serializable {

    private static final String API_URL = "https://api.gathercontent.com";
    private final Map<String, String> headers = ImmutableMap.of("Accept", "application/vnd.gathercontent.v0.5+json");
    private final Map<String, String> newEditorHeaders = ImmutableMap.of("Accept", "application/vnd.gathercontent.v0.6+json");
    private String username;
    private String apikey;
    private Boolean isNewEditor;

    private GCContext(final String username, final String apikey, final boolean isNewEditor) {
        this.username = username;
        this.apikey = apikey;
        this.isNewEditor = isNewEditor;
    }

    /**
     * Build <code>{@link GCContext}</code> object.
     *
     * @param username GatherContent Username.
     * @param apikey   GatherContent Api Key,
     * @return Created <code>{@link GCContext}</code> object.
     */
    public static GCContext build(final String username, final String apikey, final Boolean isNewEditor) {
        return new GCContext(username, apikey, isNewEditor);
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

    public Map<String, String> getNewEditorHeaders() {
        return newEditorHeaders;
    }

    public String getApiURL() {
        return API_URL;
    }

    public Boolean isNewEditor() {
        return isNewEditor;
    }

    public void setNewEditor(Boolean newEditor) {
        isNewEditor = newEditor;
    }
}
