package com.axamit.gc.core.services;

import com.axamit.gc.api.dto.*;

public abstract class AbstractPageModifier {

    protected static GCContent findContentByKey(final GCItem gcItem, final String id) {
        final GCContent gcContent = gcItem.getContent().get(id);
        if (gcContent != null) {
            return gcContent;
        }
        //look through children
        return gcItem.getContent().values().stream().filter(entry -> GCElementType.COMPONENT.equals(entry.getType()))
                .filter(entry -> entry.getComponent().containsKey(id))
                .findFirst()
                .map(entry -> entry.getComponent()
                        .get(id)).orElse(null);
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
