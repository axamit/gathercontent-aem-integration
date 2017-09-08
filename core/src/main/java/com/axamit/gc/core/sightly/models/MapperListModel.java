/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
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
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class MapperListModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperListModel.class);

    @Inject
    private GCContentApi gcContentApi;

    @Inject
    private GCConfiguration gcConfiguration;

    @Inject
    private SlingHttpServletRequest request;

    private Resource resource;

    private List<MapperModel> mappingList;

    private String typeLabel = Constants.MAPPINGS_IMPORT_TYPE_LABEL;

    private String mappingSideSelector = Constants.MAPPING_IMPORT_SELECTOR;

    /**
     * Constructor with resource initializing.
     *
     * @param request <code>SlingHttpServletRequest</code> object.
     */
    public MapperListModel(final SlingHttpServletRequest request) {
        this.resource = request.getResource();
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
        Map<String, List<GCItem>> itemsByProject = new HashMap<>();

        String[] selectors = request.getRequestPathInfo().getSelectors();
        boolean isExport = false;
        if (selectors != null) {
            for (String selector : selectors) {
                if (Constants.MAPPING_EXPORT_SELECTOR.equals(selector)) {
                    isExport = true;
                    typeLabel = Constants.MAPPINGS_EXPORT_TYPE_LABEL;
                    mappingSideSelector = Constants.MAPPING_EXPORT_SELECTOR;
                    break;
                }
            }
        }

        for (Resource mappingResource : mappingResources) {
            MapperModel mapperModel = mappingResource.adaptTo(MapperModel.class);
            if (mapperModel == null) {
                LOGGER.error("Can not adapt mapping \"{}\" to model {}", mappingResource.getPath(),
                        MapperModel.class.getName());
                continue;
            }
            String type = mapperModel.getType();
            if (isExport && Constants.MAPPING_TYPE_EXPORT.equals(type)
                    || !isExport && !Constants.MAPPING_TYPE_EXPORT.equals(type)) {
                String projectId = mapperModel.getProjectId();
                String templateId = mapperModel.getTemplateId();
                if (StringUtils.isNotEmpty(templateId)) {
                    switch (mapperModel.getMappingType()) {
                        case ENTRY_PARENT:
                        case CUSTOM_ITEM:
                            List<GCItem> gcItems;
                            try {
                                if (itemsByProject.containsKey(projectId)) {
                                    gcItems = itemsByProject.get(projectId);
                                } else {
                                    gcItems = gcContentApi.itemsByProjectId(gcContext, projectId);
                                    itemsByProject.put(projectId, gcItems);
                                }
                                for (GCItem gcItem : gcItems) {
                                    if (templateId.equals(gcItem.getId())) {
                                        mapperModel.setCustomItem(gcItem);
                                        break;
                                    }
                                }
                            } catch (GCException e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                            break;
                        case TEMPLATE:
                        default:
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
                            break;
                    }
                }
                mappingList.add(mapperModel);
            }
        }
    }

    public List<MapperModel> getMappingList() {
        return mappingList;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public String getMappingSideSelector() {
        return mappingSideSelector;
    }
}
