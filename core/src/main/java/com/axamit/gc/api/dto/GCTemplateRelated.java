/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The <code>GCTemplateRelated</code> class represents template's related.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplateRelated {

    private GCTemplateStructure structure;

    public GCTemplateStructure getStructure() {
        return structure;
    }

    public void setStructure(GCTemplateStructure structure) {
        this.structure = structure;
    }
}
