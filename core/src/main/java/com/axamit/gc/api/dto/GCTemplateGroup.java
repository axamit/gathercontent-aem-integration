/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;


/**
 * The <code>GCTemplateGroup</code> class represents template's groups.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTemplateGroup {

    private String uuid;
    private String name;
    private List<GCTemplateField> fields;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GCTemplateField> getFields() {
        return fields;
    }

    public List<GCTemplateField> getFieldsWithChildren() {
        List<GCTemplateField> result = new ArrayList<>();
        fields.forEach(gcTemplateField -> {
            if (GCElementType.COMPONENT.equals(gcTemplateField.getType())) {
                result.addAll(gcTemplateField.getComponent().getFields());
            } else {
                result.add(gcTemplateField);
            }
        });
        return result;
    }

    public void setFields(List<GCTemplateField> fields) {
        this.fields = fields;
    }
}
