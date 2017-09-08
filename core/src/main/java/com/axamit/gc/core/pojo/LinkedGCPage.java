package com.axamit.gc.core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * The <code>LinkedGCPage</code> represents information about exported/imported GC items.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkedGCPage {
    private String gcProjectId;
    private String gcItemId;
    private String gcMappingPath;

    /**
     * Default constructor.
     */
    public LinkedGCPage() {
    }

    /**
     * @param gcProjectId   GatherContent Project ID that will be associated with AEM page.
     * @param gcItemId      GatherContent Item ID that will be associated with AEM page.
     * @param gcMappingPath Path to mapping that will be associated with AEM page.
     */
    public LinkedGCPage(String gcProjectId, String gcItemId, String gcMappingPath) {
        this.gcProjectId = gcProjectId;
        this.gcItemId = gcItemId;
        this.gcMappingPath = gcMappingPath;
    }

    public String getGcProjectId() {
        return gcProjectId;
    }

    public void setGcProjectId(String gcProjectId) {
        this.gcProjectId = gcProjectId;
    }

    public String getGcItemId() {
        return gcItemId;
    }

    public void setGcItemId(String gcItemId) {
        this.gcItemId = gcItemId;
    }

    public String getGcMappingPath() {
        return gcMappingPath;
    }

    public void setGcMappingPath(String gcMappingPath) {
        this.gcMappingPath = gcMappingPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinkedGCPage that = (LinkedGCPage) o;
        return Objects.equals(gcProjectId, that.gcProjectId)
                && Objects.equals(gcItemId, that.gcItemId)
                && Objects.equals(gcMappingPath, that.gcMappingPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gcProjectId, gcItemId, gcMappingPath);
    }
}
