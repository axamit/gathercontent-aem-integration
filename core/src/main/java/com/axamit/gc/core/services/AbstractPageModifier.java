package com.axamit.gc.core.services;

import com.axamit.gc.api.dto.*;

import java.util.Map;

public abstract class AbstractPageModifier {

    protected static GCContent findContentByKey(final GCItem gcItem, final String id) {
        final GCContent gcContent = gcItem.getContent().get(id);
        if (gcContent != null) {
            return gcContent;
        }
        //look through children
        for (Map.Entry<String, GCContent> gcContentEntry : gcItem.getContent().entrySet()) {
            GCContent entry = gcContentEntry.getValue();
            if (GCElementType.COMPONENT.equals(entry.getType())) {
                if (entry.getComponent().containsKey(id)) {
                    return entry.getComponent().get(id);
                }
            }
        }
        return null;
    }

    protected static GCTemplateField findTemplateFieldByKey(final GCTemplate gcTemplate, final String id) {
        for (GCTemplateGroup group : gcTemplate.getRelated().getStructure().getGroups()) {
            final GCTemplateField fieldByKey = findFieldByKey(group, id);
            if (fieldByKey != null) {
                return fieldByKey;
            }
        }
        return null;
    }

    private static GCTemplateField findFieldByKey(final GCTemplateGroup gcTemplateGroup, final String id) {
        for (GCTemplateField gcTemplateField : gcTemplateGroup.getFields()) {
            if (gcTemplateField.getUuid().equals(id)) {
                return gcTemplateField;
            } else if (gcTemplateField.getComponent() != null) {
                return findFieldByKey(gcTemplateField.getComponent(), id);
            }
        }
        return null;
    }
}
