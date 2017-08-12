/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCFile</code> class represents file (attachment).
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/items/get-items-files/">Files</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCFile {

    private String id;
    private String userId;
    private String itemId;
    private String field;
    private String type;
    private String url;
    private String filename;
    private String size;

//    private String created_at;
//    private String updated_at;

    /**
     * @return ID.
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return User ID.
     */
    @JsonProperty("user_id")
    public String getUserId() {
        return userId;
    }

    @JsonProperty("user_id")
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    /**
     * @return Item ID.
     */
    @JsonProperty("item_id")
    public String getItemId() {
        return itemId;
    }

    @JsonProperty("item_id")
    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    /**
     * @return Field like 'abc123'.
     */
    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    /**
     * @return Type like '1'.
     */
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return URL like 'http://link.to/filename.png'.
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * @return Filename like 'original.png'.
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    /**
     * @return Size of file.
     */
    public String getSize() {
        return size;
    }

    public void setSize(final String size) {
        this.size = size;
    }
}
