/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


/**
 * The <code>GCTemplateStructure</code> class represents template's structure.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplateStructure {

    private String uuid;
    private List<GCTemplateGroup> groups;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<GCTemplateGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<GCTemplateGroup> groups) {
        this.groups = groups;
    }
}
