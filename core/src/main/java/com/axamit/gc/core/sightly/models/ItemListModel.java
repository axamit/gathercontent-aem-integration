/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCData;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCItemType;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportUpdateTableItem;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.google.common.collect.Table;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sling model class which represents table with items to process on import page.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class ItemListModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemListModel.class);

    @Inject
    private GCContentApi gcContentApi;

    @Inject
    private GCConfiguration gcConfiguration;

    @Inject
    private SlingHttpServletRequest request;

    private Resource resource;

    private List<GCProject> projects = new ArrayList<>();

    private List<ImportUpdateTableItem> itemList;

    private List<GCData> projectStatusList;

    /**
     * Constructor with resource field initialization.
     *
     * @param request org.apache.sling.api.SlingHttpServletRequest request.
     */
    public ItemListModel(final SlingHttpServletRequest request) {
        this.resource = request.getResource();
    }

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        GCContext gcContext = gcConfiguration.getGCContext(resource);
        String accountId = gcConfiguration.getAccountId(resource);
        try {
            itemList = new ArrayList<>();
            String projectId = GCUtil.getMappedProjectIdFromSelector(gcContext,
                    gcContentApi, accountId, request, this.projects, Constants.MAPPING_TYPE_IMPORT);
            if (projectId != null) {
                projectStatusList = gcContentApi.statusesByProjectId(gcContext, projectId);
                List<GCItem> allGcItems = gcContentApi.itemsByProjectId(gcContext, projectId);
                Table<MappingType, String, Set<Map<String, String>>> mappedTemplatesAndItems =
                        GCUtil.getMappedTemplates(resource, Constants.MAPPING_TYPE_IMPORT);
                Map<String, Set<Map<String, String>>> mappedTemplatesIds = mappedTemplatesAndItems.row(MappingType.TEMPLATE);
                Map<String, Set<Map<String, String>>> mappedEntriesIds = mappedTemplatesAndItems.row(MappingType.ENTRY_PARENT);
                Map<String, Set<Map<String, String>>> mappedCustomItemsIds = mappedTemplatesAndItems.row(MappingType.CUSTOM_ITEM);

                for (GCItem gcItem : allGcItems) {
                    if (mappedTemplatesIds.containsKey(gcItem.getTemplateId())) {
                        ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedTemplatesIds.get(gcItem.getTemplateId()));
                        itemList.add(listItem);
                    } else if (GCItemType.ENTRY_PARENT.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getId())) {
                        ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedEntriesIds.get(gcItem.getId()));
                        itemList.add(listItem);
                    } else if (GCItemType.ENTRY_CHILD.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getParentId())) {
                        ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedEntriesIds.get(gcItem.getParentId()));
                        itemList.add(listItem);
                    } else if (GCItemType.ITEM.equals(gcItem.getItemType()) && gcItem.getTemplateId() == null
                            && mappedCustomItemsIds.containsKey(gcItem.getId())) {
                        ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedCustomItemsIds.get(gcItem.getId()));
                        itemList.add(listItem);
                    }
                }

                itemList = GCUtil.reorderGcChildren(itemList);
                setHierarchyTitles(itemList);
            }
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ImportUpdateTableItem createImportUpdateTableItem(GCItem gcItem, Set<Map<String, String>> mappedItems)
            throws GCException {
        ImportUpdateTableItem listItem = new ImportUpdateTableItem();
        String gcTemplate = mappedItems.iterator().next().get(Constants.GC_TEMPLATE_NAME_PN);
        listItem.setGcTemplate(gcTemplate);
        listItem.setJsonInformation(JSONUtil.fromObjectToJsonString(mappedItems));
        listItem.setId(gcItem.getId());
        listItem.setTitle(gcItem.getName());
        listItem.setValidName(GCUtil.createValidName(gcItem.getName()));
        listItem.setStatus(gcItem.getStatus().getData().getName());
        listItem.setParentId(gcItem.getParentId());
        listItem.setColor(gcItem.getStatus().getData().getColor());
        return listItem;
    }

    private void setHierarchyTitles(List<ImportUpdateTableItem> items) {
        for (ImportUpdateTableItem importUpdateTableItem : items) {
            importUpdateTableItem.setHierarchyTitle(GCUtil.getHierarchyName(items, importUpdateTableItem.getParentId(),
                    importUpdateTableItem.getTitle()));
        }
    }

    public List<GCProject> getProjects() {
        return projects;
    }

    public List<ImportUpdateTableItem> getItemList() {
        return itemList;
    }

    public List<GCData> getProjectStatusList() {
        return projectStatusList;
    }

    public String getDefaultImportPath() {
        return Constants.DEFAULT_IMPORT_PATH;
    }

}
