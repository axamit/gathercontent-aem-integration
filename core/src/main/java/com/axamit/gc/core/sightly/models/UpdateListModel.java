/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTime;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportUpdateTableItem;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.query.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Sling model class which represents table with items to process on update page.
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class UpdateListModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateListModel.class);

    private static final String IMPORTED_PAGES_QUERY =
            "SELECT * FROM [cq:PageContent] AS pageContent WHERE ISDESCENDANTNODE(pageContent , '/content')"
                    + " AND [isGCImportedPage] = true"
                    + " AND [GCProjectId]='%s'";

    @Inject
    private GCContentApi gcContentApi;

    @Inject
    private GCConfiguration gcConfiguration;

    @Inject
    private SlingHttpServletRequest request;

    private Resource resource;

    private List<GCProject> projects = new ArrayList<>();

    private List<ImportUpdateTableItem> itemList;

    /**
     * Constructor with resource initializing.
     *
     * @param request <code>{@link SlingHttpServletRequest}</code> object.
     */
    public UpdateListModel(final SlingHttpServletRequest request) {
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
            String projectId =
                    GCUtil.getMappedProjectIdFromSelector(gcContext, gcContentApi, accountId, request, projects);
            if (projectId != null) {
                List<GCItem> allGcItems = gcContentApi.itemsByProjectId(gcContext, projectId);
                Map<String, Set<Map<String, String>>> mappedTemplatesIds = GCUtil.getMappedTemplates(resource);
                Map<String, Map<String, Resource>> importedPages = getImportedPages(projectId);
                for (GCItem gcItem : allGcItems) {
                    if (importedPages.containsKey(gcItem.getId())
                            && mappedTemplatesIds.containsKey(gcItem.getTemplateId())) {
                        for (Resource importedPageResource : importedPages.get(gcItem.getId()).values()) {
                            PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
                            String importPath = pageManager.getContainingPage(importedPageResource).getPath();
                            ValueMap valueMap = importedPageResource.getValueMap();
                            Calendar calendar = valueMap.get(NameConstants.PN_PAGE_LAST_MOD, Calendar.class);
                            String mappingPath = valueMap.get(Constants.GC_IMPORTED_PAGE_MAPPING_PATH, String.class);
                            GCTime updatedAt = gcItem.getUpdatedAt();
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.OUTPUT_DATE_FORMAT);
                            SimpleDateFormat itemDateFormat = new SimpleDateFormat(Constants.ITEM_DATE_FORMAT);
                            outputDateFormat.setTimeZone(TimeZone.getTimeZone(updatedAt.getTimezone()));
                            Date date = null;
                            try {
                                date = itemDateFormat.parse(updatedAt.getDate());
                            } catch (ParseException e) {
                                LOGGER.error("Fail to parse last updated date in GATHERCONTENT", e);
                            }
                            ImportUpdateTableItem listItem = new ImportUpdateTableItem();
                            listItem.setId(gcItem.getId());
                            listItem.setTitle(gcItem.getName());
                            listItem.setStatus(gcItem.getStatus().getData().getName());
                            Set<Map<String, String>> mappedtemplates = mappedTemplatesIds.get(gcItem.getTemplateId());
                            String gcTemplate = mappedtemplates.iterator().next().get(Constants.GC_TEMPLATE_NAME_PN);
                            listItem.setGcTemplate(gcTemplate);
                            listItem.setMappingPath(mappingPath);
                            listItem.setImportPath(importPath);
                            listItem.setAemUpdateDate(outputDateFormat.format(calendar.getTime()));
                            listItem.setGcUpdateDate(date != null ? outputDateFormat.format(date) : null);
                            listItem.setColor(gcItem.getStatus().getData().getColor());
                            itemList.add(listItem);
                        }
                    }
                }
            }
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private Map<String, Map<String, Resource>> getImportedPages(final String projectId) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        Iterator<Resource> importedPagesResources =
                resourceResolver.findResources(String.format(IMPORTED_PAGES_QUERY, projectId), Query.JCR_SQL2);
        Map<String, Map<String, Resource>> importedPages = new HashMap<>();
        while (importedPagesResources.hasNext()) {
            Resource importedPageResource = importedPagesResources.next();
            String itemId = importedPageResource.getValueMap().get(Constants.GC_IMPORTED_PAGE_ITEM_ID, String.class);
            if (itemId != null && !itemId.isEmpty()) {
                Map<String, Resource> resources = importedPages.get(itemId);
                if (resources == null) {
                    resources = new HashMap<>();
                    resources.put(importedPageResource.getPath(), importedPageResource);
                    importedPages.put(itemId, resources);
                } else {
                    resources.put(importedPageResource.getPath(), importedPageResource);
                }
            }
        }
        return importedPages;
    }

    public List<GCProject> getProjects() {
        return projects;
    }

    public List<ImportUpdateTableItem> getItemList() {
        return itemList;
    }

}
