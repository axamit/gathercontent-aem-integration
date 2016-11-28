/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sling model class which represents table with mapping list on mappings page.
 */
@Model(adaptables = Resource.class)
public final class MapperListModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperListModel.class);

    @Inject
    private GCContentApi gcContentApi;

    @Inject
    private GCConfiguration gcConfiguration;

    private Resource resource;

    private List<MapperModel> mappingList;

    /**
     * Constructor with resource field initialization.
     * @param resource org.apache.sling.api.resource.Resource resource.
     */
    public MapperListModel(final Resource resource) {
        this.resource = resource;
    }
    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        mappingList = new ArrayList<>();
        Iterable<Resource> mappingResources = resource.getChildren();
        GCContext gcContext = gcConfiguration.getGCContext(resource);
        Map<String, List<GCTemplate>> templatesByProject = new HashMap<>();
        for (Resource mappingResource : mappingResources) {
            MapperModel mapperModel = mappingResource.adaptTo(MapperModel.class);
            String projectId = mapperModel.getProjectId();
            String templateId = mapperModel.getTemplateId();
            List<GCTemplate> gcTemplates;
            try {
                if (templatesByProject.containsKey(projectId)) {
                    gcTemplates = templatesByProject.get(projectId);
                } else {
                    gcTemplates = gcContentApi.templates(gcContext, projectId);
                    templatesByProject.put(projectId, gcTemplates);
                }
                for (GCTemplate gcTemplate : gcTemplates) {
                    if (templateId.equals(gcTemplate.getId())) {
                        mapperModel.setGctemplate(gcTemplate);
                        break;
                    }
                }
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
            mappingList.add(mapperModel);
        }
    }

    public List<MapperModel> getMappingList() {
        return mappingList;
    }
}
