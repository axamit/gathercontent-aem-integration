/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCFile</code> class represents file (attachment).
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class GCFile {

    private String fileId;
    private String filename;
    private String mimeType;
    private String url;
    private String optimisedImageUrl;
    private String downloadUrl;
    private Integer size;
    private String altText;

    @JsonProperty("file_id")
    public String getFileId() {
        return fileId;
    }

    @JsonProperty("file_id")
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonProperty("mime_type")
    public String getMimeType() {
        return mimeType;
    }

    @JsonProperty("mime_type")
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("optimised_image_url")
    public String getOptimisedImageUrl() {
        return optimisedImageUrl;
    }

    @JsonProperty("optimised_image_url")
    public void setOptimisedImageUrl(String optimisedImageUrl) {
        this.optimisedImageUrl = optimisedImageUrl;
    }

    @JsonProperty("download_url")
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @JsonProperty("download_url")
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @JsonProperty("alt_text")
    public String getAltText() {
        return altText;
    }

    @JsonProperty("alt_text")
    public void setAltText(String altText) {
        this.altText = altText;
    }
}
