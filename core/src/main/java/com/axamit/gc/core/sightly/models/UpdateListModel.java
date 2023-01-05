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
import com.axamit.gc.core.pojo.LinkedGCPage;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.query.Query;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Sling model class which represents table with items to process on update page.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class UpdateListModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateListModel.class);

    private static final String IMPORTED_PAGES_QUERY =
            "SELECT * FROM [cq:PageContent] AS pageContent WHERE ISDESCENDANTNODE(pageContent , '/content')"
                    + " AND [isGCImportedPage] = true"
                    + " AND [GCProjectId]='%s'";

    private static final String EXPORTED_PAGES_QUERY =
            "SELECT * FROM [cq:PageContent] AS pageContent WHERE ISDESCENDANTNODE(pageContent , '/content')"
                    + " AND [isGCExportedPage] = true";

    private List<GCStatus> projectStatusList = new ArrayList<>();
    private final List<GCProject> projects = new ArrayList<>();
    private final List<ImportUpdateTableItem> itemList = new ArrayList<>();

    /**
     * Constructor with resource initializing.
     *
     * @param request         <code>{@link SlingHttpServletRequest}</code> object.
     * @param gcContentApi    Content API
     * @param gcConfiguration Configuration
     */
    @Inject
    public UpdateListModel(final SlingHttpServletRequest request,
                           final GCContentApi gcContentApi,
                           final GCContentNewApi gcContentNewApi,
                           final GCConfiguration gcConfiguration) {
        final Resource resource = request.getResource();
        final GCContext gcContext = gcConfiguration.getGCContext(resource);
        final Integer accountId = gcConfiguration.getAccountId(resource);

        try {
            final String[] selectors = request.getRequestPathInfo().getSelectors();
            final String side = Arrays.asList(selectors).contains(Constants.MAPPING_TYPE_EXPORT)
                    ? Constants.MAPPING_TYPE_EXPORT
                    : Constants.MAPPING_TYPE_IMPORT;

            final Integer projectId =
                    GCUtil.getMappedProjectIdFromSelector(gcContext, gcContentApi, accountId, request, projects, null);
            if (projectId != null && projectId != 0) {
                projectStatusList = gcContentApi.statusesByProjectId(gcContext, projectId);
                final List<GCFolder> gcFolders = gcContentNewApi.foldersByProjectId(gcContext, projectId);
                final List<GCItem> allGcItems = gcContentNewApi.itemsByProjectId(gcContext, projectId);
                final Table<MappingType, Integer, Set<Map<String, String>>> mappedTemplatesAndItems =
                        GCUtil.getMappedTemplates(resource, null);
                final Map<Integer, Set<Map<String, String>>> mappedTemplatesIds =
                        mappedTemplatesAndItems.row(MappingType.TEMPLATE);
                //TODO
//                Map<String, Set<Map<String, String>>> mappedEntriesIds =
//                        mappedTemplatesAndItems.row(MappingType.ENTRY_PARENT);

                //Map<String, Set<Map<String, String>>> mappedCustomItemsIds = mappedTemplatesAndItems.row
                // (MappingType.CUSTOM_ITEM);
                final Map<Integer, Set<UpdateResourceUnit>> importedPages = getImportedExportedPages(resource, projectId,
                        String.format(IMPORTED_PAGES_QUERY, projectId), StringUtils.EMPTY,
                        Constants.GC_IMPORTED_PAGE_PROJECT_ID, Constants.GC_IMPORTED_PAGE_ITEM_ID,
                        Constants.GC_IMPORTED_PAGE_MAPPING_PATH);
                final Map<Integer, Set<UpdateResourceUnit>> exportedPages = getImportedExportedPages(resource, projectId,
                        EXPORTED_PAGES_QUERY, Constants.GC_EXPORTED_PAGES_MAP,
                        Constants.GC_EXPORTED_PAGE_PROJECT_ID, Constants.GC_EXPORTED_PAGE_ITEM_ID,
                        Constants.GC_EXPORTED_PAGE_MAPPING_PATH);
                final String slug = getSlug(gcContentApi, gcContext, accountId);

                for (GCItem gcItem : allGcItems) {
                    final Set<UpdateResourceUnit> importedPagesForItem = importedPages.get(gcItem.getId());
                    final Set<UpdateResourceUnit> exportedPagesForItem = exportedPages.get(gcItem.getId());
                    if ((importedPagesForItem != null || exportedPagesForItem != null)
                            && (mappedTemplatesIds.containsKey(gcItem.getTemplateId())
                            //TODO
                            /*|| GCItemType.ENTRY_PARENT.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getId()) && Constants.MAPPING_TYPE_IMPORT.equals(side)
                            || GCItemType.ENTRY_CHILD.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getParentId()) && Constants.MAPPING_TYPE_IMPORT.equals(side)
                            || GCItemType.ITEM.equals(gcItem.getItemType()) && gcItem.getTemplateId() == null*/)) {
                        switch (side) {
                            case Constants.MAPPING_TYPE_EXPORT:
                                addItemsFromPages(exportedPagesForItem, gcItem, slug);
                                break;
                            case Constants.MAPPING_TYPE_IMPORT:
                            default:
                                addItemsFromPages(importedPagesForItem, gcItem, slug);
                        }
                    }
                }
//                itemList = GCUtil.reorderGcChildren(itemList);
                GCUtil.setHierarchyTitles(gcFolders, itemList);
            }
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void addItemsFromPages(Set<UpdateResourceUnit> updateResourceUnits, GCItem gcItem, String slug) {
        if (updateResourceUnits != null) {
            updateResourceUnits.stream().map(updateResourceUnit -> createImportUpdateTableItem(gcItem, updateResourceUnit, slug))
                    .filter(Objects::nonNull)
                    .forEach(itemList::add);
        }
    }

    private ImportUpdateTableItem createImportUpdateTableItem(GCItem gcItem, UpdateResourceUnit updateResourceUnit,
                                                                     String slug) {
        Resource importedPageResource = updateResourceUnit.getPageResource();
        ResourceResolver resourceResolver = importedPageResource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        String importPath = pageManager.getContainingPage(importedPageResource).getPath();
        //! OSGI configurable mask like "https://{0}.gathercontent.com/item/" would be great.
        String gcPath = "https://" + slug + ".gathercontent.com/item/" + gcItem.getId();
        ValueMap valueMap = importedPageResource.getValueMap();
        Calendar calendar = valueMap.get(NameConstants.PN_PAGE_LAST_MOD, Calendar.class);
        String mappingPath = updateResourceUnit.linkedGCPage.getGcMappingPath();
        Resource mappingResource = resourceResolver.getResource(mappingPath);
        if (mappingResource == null) {
            //! Log
            return null;
        }
        MapperModel mapperModel = mappingResource.adaptTo(MapperModel.class);
        if (mapperModel == null) {
            //! Log
            return null;
        }
        SimpleDateFormat outputSimpleDateFormatWithTimeZone =
                GCUtil.getOutputSimpleDateFormatWithTimeZone(calendar, Constants.OUTPUT_DATE_FORMAT);
        ImportUpdateTableItem listItem = new ImportUpdateTableItem();
        listItem.setGcTemplate(mapperModel.getTemplateName());
        listItem.setId(gcItem.getId());
        listItem.setFolderUuid(gcItem.getFolderUuid());
        listItem.setTitle(gcItem.getName());
        listItem.setMappingName(mapperModel.getMappingName());
        listItem.setMappingPath(mappingPath);
        listItem.setImportPath(importPath);
        listItem.setGcPath(gcPath);
        listItem.setAemUpdateDate(outputSimpleDateFormatWithTimeZone.format(calendar.getTime()));
        listItem.setGcUpdateDate(gcItem.getUpdatedAt());

        projectStatusList.stream()
                .filter(status -> status.getId().equals(gcItem.getStatusId()))
                .findAny()
                .ifPresent(gcStatus -> {
                    listItem.setStatus(gcStatus.getName());
                    listItem.setColor(gcStatus.getColor());
                });
        return listItem;
    }

    private static Map<Integer, Set<UpdateResourceUnit>> getImportedExportedPages(final Resource resource,
                                                                                 final Integer projectId,
                                                                                 final String query,
                                                                                 final String linkedPagesMapPN,
                                                                                 final String projectIdPN,
                                                                                 final String itemIdPN,
                                                                                 final String mappingPathPN) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        Iterator<Resource> linkedPagesResources =
                resourceResolver.findResources(query, Query.JCR_SQL2);
        Map<Integer, Set<UpdateResourceUnit>> linkedResources = new HashMap<>();
        while (linkedPagesResources.hasNext()) {
            Resource linkedPageResource = linkedPagesResources.next();
            Map<Integer, LinkedGCPage> linkedGCPages =
                    GCUtil.getLinkedGCPages(linkedPageResource.getValueMap(),
                            linkedPagesMapPN, projectIdPN, itemIdPN, mappingPathPN);
            linkedGCPages.values().stream().filter(linkedGCPage -> projectId.equals(linkedGCPage.getGcProjectId())).forEach(linkedGCPage -> {
                UpdateResourceUnit updateResourceUnit = new UpdateResourceUnit(linkedPageResource, linkedGCPage);
                Set<UpdateResourceUnit> updateResourceUnits = linkedResources.computeIfAbsent(linkedGCPage.getGcItemId(), k -> new HashSet<>());
                updateResourceUnits.add(updateResourceUnit);
            });
        }
        return linkedResources;
    }

    private static String getSlug(GCContentApi gcContentApi, final GCContext gcContext,
                                  final Integer accountId) throws GCException {
        List<GCAccount> gcAccounts = gcContentApi.accounts(gcContext);
        return gcAccounts.stream().filter(gcAccount -> gcAccount.getId().equals(accountId)).findFirst().map(GCAccount::getSlug).orElse(null);

    }

    public List<GCProject> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    public List<ImportUpdateTableItem> getItemList() {
        return Collections.unmodifiableList(itemList);
    }

    /**
     * Class represent unit for updating.
     */
    static class UpdateResourceUnit {
        private Resource pageResource;
        private LinkedGCPage linkedGCPage;

        /**
         * Public constructor.
         *
         * @param pageResource AEM page Resource.
         * @param linkedGCPage LinkedGCPage.
         */
        UpdateResourceUnit(Resource pageResource, LinkedGCPage linkedGCPage) {
            this.pageResource = pageResource;
            this.linkedGCPage = linkedGCPage;
        }

        public Resource getPageResource() {
            return pageResource;
        }

        public void setPageResource(Resource pageResource) {
            this.pageResource = pageResource;
        }

        public LinkedGCPage getLinkedGCPage() {
            return linkedGCPage;
        }

        public void setLinkedGCPage(LinkedGCPage linkedGCPage) {
            this.linkedGCPage = linkedGCPage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UpdateResourceUnit that = (UpdateResourceUnit) o;
            return Objects.equals(pageResource.getPath(), that.pageResource.getPath())
                    && Objects.equals(linkedGCPage, that.linkedGCPage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pageResource.getPath(), linkedGCPage);
        }
    }
}
