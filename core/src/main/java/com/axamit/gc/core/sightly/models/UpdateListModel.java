/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCItemType;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTime;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportUpdateTableItem;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                    + " AND [isGCExportedPage] = true"
                    + " AND [GCProjectId]='%s'";

    private final List<GCProject> projects = new ArrayList<>();
    private List<ImportUpdateTableItem> itemList = new ArrayList<>();

    /**
     * Constructor with resource initializing.
     *
     * @param request         <code>{@link SlingHttpServletRequest}</code> object.
     * @param gcContentApi    Content API
     * @param gcConfiguration Configuration
     */
    @Inject
    public UpdateListModel(final SlingHttpServletRequest request, GCContentApi gcContentApi,
                           GCConfiguration gcConfiguration) {
        final Resource resource = request.getResource();
        final GCContext gcContext = gcConfiguration.getGCContext(resource);
        final String accountId = gcConfiguration.getAccountId(resource);

        try {
            final String[] selectors = request.getRequestPathInfo().getSelectors();
            final String side = Arrays.asList(selectors).contains(Constants.MAPPING_TYPE_EXPORT)
                    ? Constants.MAPPING_TYPE_EXPORT
                    : Constants.MAPPING_TYPE_IMPORT;

            final String projectId =
                    GCUtil.getMappedProjectIdFromSelector(gcContext, gcContentApi, accountId, request, projects, null);
            if (projectId != null) {
                List<GCItem> allGcItems = gcContentApi.itemsByProjectId(gcContext, projectId);
                Table<MappingType, String, Set<Map<String, String>>> mappedTemplatesAndItems =
                        GCUtil.getMappedTemplates(resource, null);
                Map<String, Set<Map<String, String>>> mappedTemplatesIds =
                        mappedTemplatesAndItems.row(MappingType.TEMPLATE);
                Map<String, Set<Map<String, String>>> mappedEntriesIds =
                        mappedTemplatesAndItems.row(MappingType.ENTRY_PARENT);
                //Map<String, Set<Map<String, String>>> mappedCustomItemsIds = mappedTemplatesAndItems.row
                // (MappingType.CUSTOM_ITEM);
                Map<String, Map<String, Resource>> importedPages = getImportedExportedPages(resource, projectId, IMPORTED_PAGES_QUERY, Constants.GC_IMPORTED_PAGE_ITEM_ID);
                Map<String, Map<String, Resource>> exportedPages = getImportedExportedPages(resource, projectId, EXPORTED_PAGES_QUERY, Constants.GC_EXPORTED_PAGE_ITEM_ID);
                String slug = getSlug(gcContentApi, gcContext, accountId);

                for (GCItem gcItem : allGcItems) {
                    Map<String, Resource> importedPagesForItem = importedPages.get(gcItem.getId());
                    Map<String, Resource> exportedPagesForItem = exportedPages.get(gcItem.getId());
                    if ((importedPagesForItem != null || exportedPagesForItem != null)
                            && (mappedTemplatesIds.containsKey(gcItem.getTemplateId())
                            || GCItemType.ENTRY_PARENT.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getId()) && Constants.MAPPING_TYPE_IMPORT.equals(side)
                            || GCItemType.ENTRY_CHILD.equals(gcItem.getItemType()) && mappedEntriesIds.containsKey(gcItem.getParentId()) && Constants.MAPPING_TYPE_IMPORT.equals(side)
                            || GCItemType.ITEM.equals(gcItem.getItemType()) && gcItem.getTemplateId() == null)) {
                        switch (side) {
                            case Constants.MAPPING_TYPE_EXPORT:
                                addItemsFromPages(exportedPagesForItem, Constants.GC_EXPORTED_PAGE_MAPPING_PATH,
                                        gcItem, slug);
                                break;
                            case Constants.MAPPING_TYPE_IMPORT:
                            default:
                                addItemsFromPages(importedPagesForItem, Constants.GC_IMPORTED_PAGE_MAPPING_PATH,
                                        gcItem, slug);
                        }
                    }
                }
                itemList = GCUtil.reorderGcChildren(itemList);
                setHierarchyTitles(itemList);
            }
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void addItemsFromPages(Map<String, Resource> pagesForItem, String pageMappingPath, GCItem gcItem,
                                   String slug) {
        if (pagesForItem != null) {
            for (Resource pageResource : pagesForItem.values()) {
                ImportUpdateTableItem listItem = createImportUpdateTableItem(gcItem, pageResource, slug, pageMappingPath);
                if (listItem != null) {
                    itemList.add(listItem);
                }
            }
        }
    }

    private static ImportUpdateTableItem createImportUpdateTableItem(GCItem gcItem, Resource importedPageResource,
                                                                     String slug,
                                                                     String pageMappingPropertyName) {
        ResourceResolver resourceResolver = importedPageResource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        String importPath = pageManager.getContainingPage(importedPageResource).getPath();
        //! OSGI configurable mask like "https://{0}.gathercontent.com/item/" would be great.
        String gcPath = "https://" + slug + ".gathercontent.com/item/" + gcItem.getId();
        ValueMap valueMap = importedPageResource.getValueMap();
        Calendar calendar = valueMap.get(NameConstants.PN_PAGE_LAST_MOD, Calendar.class);
        String mappingPath = valueMap.get(pageMappingPropertyName, String.class);
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
        GCTime updatedAt = gcItem.getUpdatedAt();
        SimpleDateFormat outputSimpleDateFormatWithTimeZone =
                GCUtil.getOutputSimpleDateFormatWithTimeZone(updatedAt, Constants.OUTPUT_DATE_FORMAT);
        Date date = GCUtil.getDateFromGCItem(updatedAt);
        ImportUpdateTableItem listItem = new ImportUpdateTableItem();
        listItem.setGcTemplate(mapperModel.getTemplateName());
        listItem.setId(gcItem.getId());
        listItem.setParentId(gcItem.getParentId());
        listItem.setTitle(gcItem.getName());
        listItem.setStatus(gcItem.getStatus().getData().getName());
        listItem.setMappingName(mapperModel.getMappingName());
        listItem.setMappingPath(mappingPath);
        listItem.setImportPath(importPath);
        listItem.setGcPath(gcPath);
        listItem.setAemUpdateDate(outputSimpleDateFormatWithTimeZone.format(calendar.getTime()));
        listItem.setGcUpdateDate(date != null ? outputSimpleDateFormatWithTimeZone.format(date) : null);
        listItem.setColor(gcItem.getStatus().getData().getColor());
        return listItem;

    }

    private static Map<String, Map<String, Resource>> getImportedExportedPages(Resource resource,
                                                                               final String projectId, final String query,
                                                                               final String itemIdPropertyName) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        Iterator<Resource> importedPagesResources =
                resourceResolver.findResources(String.format(query, projectId), Query.JCR_SQL2);
        Map<String, Map<String, Resource>> importedPages = new HashMap<>();
        while (importedPagesResources.hasNext()) {
            Resource importedPageResource = importedPagesResources.next();
            String itemId = importedPageResource.getValueMap().get(itemIdPropertyName, String.class);
            if (StringUtils.isNotEmpty(itemId)) {
                Map<String, Resource> resources = importedPages.get(itemId);
                if (resources == null) {
                    resources = new HashMap<>();
                    importedPages.put(itemId, resources);
                }

                resources.put(importedPageResource.getPath(), importedPageResource);
            }
        }
        return importedPages;
    }

    private static String getSlug(GCContentApi gcContentApi, final GCContext gcContext,
                                  final String accountId) throws GCException {
        List<GCAccount> gcAccounts = gcContentApi.accounts(gcContext);
        for (GCAccount gcAccount : gcAccounts) {
            if (gcAccount.getId().equals(accountId)) {
                return gcAccount.getSlug();
            }
        }

        return null;
    }

    public List<GCProject> getProjects() {
        return projects;
    }

    public List<ImportUpdateTableItem> getItemList() {
        return itemList;
    }

    private void setHierarchyTitles(List<ImportUpdateTableItem> items) {
        for (ImportUpdateTableItem importUpdateTableItem : items) {
            importUpdateTableItem.setHierarchyTitle(GCUtil.getHierarchyName(items, importUpdateTableItem.getParentId(),
                    importUpdateTableItem.getTitle()));
        }
    }
}
