/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.*;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.api.services.GCContentNewApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportUpdateTableItem;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

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
    private GCContentNewApi gcContentNewApi;

    @Inject
    private GCConfiguration gcConfiguration;

    @Inject
    private SlingHttpServletRequest request;

    private final Resource resource;

    private final List<GCProject> projects = new ArrayList<>();

    private List<ImportUpdateTableItem> itemList;

    private List<GCStatus> projectStatusList;

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
        final GCContext gcContext = gcConfiguration.getGCContext(resource);
        final Integer accountId = gcConfiguration.getAccountId(resource);
        try {
            itemList = new ArrayList<>();
            final Integer projectId = GCUtil.getMappedProjectIdFromSelector(gcContext, gcContentApi, accountId, request, this.projects, Constants.MAPPING_TYPE_IMPORT);
            if (projectId != null && projectId != 0) {
                projectStatusList = gcContentApi.statusesByProjectId(gcContext, projectId);
                final List<GCItem> allGcItems = gcContentNewApi.itemsByProjectId(gcContext, projectId);
                final List<GCFolder> gcFolders = gcContentNewApi.foldersByProjectId(gcContext, projectId);
                final GCFolder rootFolder = GCUtil.buildFolderTree(gcFolders);
                final Table<MappingType, Integer, Set<Map<String, String>>> mappedTemplatesAndItems = GCUtil.getMappedTemplates(resource, Constants.MAPPING_TYPE_IMPORT);

                final Map<Integer, Set<Map<String, String>>> mappedTemplatesIds = mappedTemplatesAndItems.row(MappingType.TEMPLATE);
//                final Map<Integer, Set<Map<String, String>>> mappedEntriesIds = mappedTemplatesAndItems.row(MappingType.ENTRY_PARENT);
//                final Map<Integer, Set<Map<String, String>>> mappedCustomItemsIds = mappedTemplatesAndItems.row(MappingType.CUSTOM_ITEM);

                for (GCItem gcItem : allGcItems) {
                    if (mappedTemplatesIds.containsKey(gcItem.getTemplateId())) {
                        final ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedTemplatesIds.get(gcItem.getTemplateId()));
                        itemList.add(listItem);
                        //TODO
//                    }
//                    else if (GCItemType.ENTRY_PARENT.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getId())) {
//                        ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedEntriesIds.get(gcItem.getId()));
//                        itemList.add(listItem);
//                    } else if (GCItemType.ENTRY_CHILD.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getParentId())) {
//                        ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedEntriesIds.get(gcItem.getParentId()));
//                        itemList.add(listItem);
//                    } else if (/*GCItemType.ITEM.equals(gcItem.getItemType()) &&*/ gcItem.getTemplateId() == null && mappedCustomItemsIds.containsKey(gcItem.getId())) {
//                        final ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, mappedCustomItemsIds.get(gcItem.getId()));
//                        itemList.add(listItem);
                    }
                }

                if (false && rootFolder != null) { //hierarchical view of items in folders - disabled, improve later
                    final List<ImportUpdateTableItem> newList = new ArrayList<>();
                    buildListOfImportUpdateTableItems(rootFolder, itemList, newList, 0);
                    itemList = newList;
                } else { //show only items
                    GCUtil.setHierarchyTitles(gcFolders, itemList);
                }
            }
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private ImportUpdateTableItem createImportUpdateTableItem(GCItem gcItem, Set<Map<String, String>> mappedItems) throws GCException {
        final ImportUpdateTableItem listItem = new ImportUpdateTableItem();
        final String gcTemplate = mappedItems.iterator().next().get(Constants.GC_TEMPLATE_NAME_PN);

        listItem.setGcTemplate(gcTemplate);
        listItem.setJsonInformation(JSONUtil.fromObjectToJsonString(mappedItems));
        listItem.setId(gcItem.getId());
        listItem.setTitle(gcItem.getName());
        listItem.setValidName(GCUtil.createValidName(gcItem.getName()));
        listItem.setFolderUuid(gcItem.getFolderUuid());
        projectStatusList.stream()
                .filter(status -> status.getId().equals(gcItem.getStatusId()))
                .findAny()
                .ifPresent(gcStatus -> {
                    listItem.setStatus(gcStatus.getName());
                    listItem.setColor(gcStatus.getColor());
                });
        return listItem;
    }

    private void buildListOfImportUpdateTableItems(final GCFolder rootFolder, final List<ImportUpdateTableItem> items, final List<ImportUpdateTableItem> newList, int level) {
        final ImportUpdateTableItem listItem = new ImportUpdateTableItem();
        listItem.setTitle(StringUtils.repeat(" ", level * 4) + rootFolder.getName());
        listItem.setValidName(GCUtil.createValidName(rootFolder.getName()));
        newList.add(listItem);

        if ("project-root".equals(rootFolder.getType())) {
            final List<ImportUpdateTableItem> rootFolderItems = items.stream()
                    .filter(item -> item.getFolderUuid() == null)
                    .peek(item -> item.setHierarchyTitle(rootFolder.getName()))
                    .peek(item -> item.setTitle(StringUtils.repeat(" ", (level + 1) * 4) + item.getTitle()))
                    .collect(Collectors.toList());
            newList.addAll(rootFolderItems);
        }

        final List<ImportUpdateTableItem> folderItems = items.stream()
                .filter(item -> item.getFolderUuid() != null)
                .filter(item -> item.getFolderUuid().equals(rootFolder.getUuid()))
                .peek(item -> item.setHierarchyTitle(rootFolder.getName()))
                .peek(item -> item.setTitle(StringUtils.repeat(" ", (level + 1) * 4) + item.getTitle()))
                .collect(Collectors.toList());
        newList.addAll(folderItems);

        if (rootFolder.getFolders() != null && !rootFolder.getFolders().isEmpty()) {
            rootFolder.getFolders().forEach(gcFolder -> buildListOfImportUpdateTableItems(gcFolder, items, newList, level + 1));
        }
    }

    public List<GCProject> getProjects() {
        return ImmutableList.copyOf(projects);
    }

    public List<ImportUpdateTableItem> getItemList() {
        return ImmutableList.copyOf(itemList);
    }

    public List<GCStatus> getProjectStatusList() {
        return ImmutableList.copyOf(projectStatusList);
    }

}
