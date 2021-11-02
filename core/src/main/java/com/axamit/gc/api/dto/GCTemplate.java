/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The <code>GCTemplate</code> class represents template.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplate {

    private GCTemplateData data;

    private GCTemplateRelated related;

    public GCTemplateData getData() {
        return data;
    }

    public void setData(GCTemplateData data) {
        this.data = data;
    }

    public GCTemplateRelated getRelated() {
        return related;
    }

    public void setRelated(GCTemplateRelated related) {
        this.related = related;
    }
}
