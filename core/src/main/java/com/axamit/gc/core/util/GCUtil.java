/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTime;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.pojo.helpers.GCHierarchySortable;
import com.axamit.gc.core.pojo.helpers.TreeNode;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * The <code>GCUtil</code> is an utility class presenting functionality using across whole application
 * to perform operations like getting mapped projects, mappings from repository etc.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum GCUtil {
    /*INSTANCE*/;
    private static final Logger LOGGER = LoggerFactory.getLogger(GCUtil.class);

    private static final String NON_EMPTY_MAPPING_QUERY =
            "SELECT * FROM [nt:unstructured] AS mapping WHERE ISDESCENDANTNODE(mapping, '%s')"
                    + " AND [sling:resourceType]='gathercontent/components/content/mapping'"
                    + " AND [templateId] <> ''"
                    + " AND [mapperStr] <> '' %s";

    private static final String MAPPINGS_BY_PROJECTID_QUERY =
            "SELECT * FROM [nt:unstructured] AS mapping WHERE ISDESCENDANTNODE(mapping, '%s')"
                    + " AND [sling:resourceType]='gathercontent/components/content/mapping'"
                    + " AND [projectId]=%s"
                    + " AND [templateId] <> ''"
                    + " AND [mapperStr] <> '' %s";

    private static final String EXPORT_MAPPING_QUERY_PREDICATE = "AND [type]='export'";
    private static final String IMPORT_MAPPING_QUERY_PREDICATE = "AND ([type] IS NULL OR [type]='import')";
    private static final Map<String, String> SIDE_PREDICATES = ImmutableMap
            .of(Constants.MAPPING_TYPE_EXPORT, EXPORT_MAPPING_QUERY_PREDICATE, Constants.MAPPING_TYPE_IMPORT,
                    IMPORT_MAPPING_QUERY_PREDICATE);

    /**
     * Build a list with hierarchical tree representations of items, when all 'parent' items have links to 'children'.
     *
     * @param importItemList <code>List</code> to order.
     * @return Ordered <code>List</code> of <code>ImportItem</code> in hierarchical tree manner.
     */
    public static List<ImportItem> reorderToTree(final Iterable<ImportItem> importItemList) {
        List<ImportItem> tree = new ArrayList<>();
        Collection<String> itemIdsWithParents = new HashSet<>();

        for (ImportItem importItem : importItemList) {
            //if item have no parentId
            if ("0".equals(importItem.getParentId())) {
                tree.add(importItem);
                itemIdsWithParents.add(importItem.getItemId());
            } else { //if item have parentId
                for (ImportItem parentImportItem : importItemList) {
                    if (importItem.getParentId().equals(parentImportItem.getItemId())
                            && importItem.getImportPath().startsWith(parentImportItem.getImportPath())) {
                        parentImportItem.getChildren().add(importItem);
                        itemIdsWithParents.add(importItem.getItemId());
                    }
                }
            }
        }
        for (ImportItem importItem : importItemList) {
            if (!itemIdsWithParents.contains(importItem.getItemId())) {
                tree.add(importItem);
            }
        }
        return tree;
    }

    /**
     * Rewrite/recalculate import paths for children of choose <code>ImportItem</code>.
     *
     * @param importItem <code>ImportItem</code> whose children import paths need to be recalculated.
     */
    public static void rewriteChildrenImportPaths(final ImportItem importItem) {
        for (ImportItem innerImportItem : importItem.getChildren()) {
            innerImportItem.setImportPath(importItem.getImportPath() + "/"
                    + GCUtil.createValidName(importItem.getTitle()));
            rewriteChildrenImportPaths(innerImportItem);
        }
    }

    /**
     * Receive collection of mapped templates in current cloudservice configuration.
     *
     * @param resource Resource of current cloudservice configuration or any of its children resources.
     * @param side     <code>Constants.MAPPING_TYPE_IMPORT</code>, <code>Constants.MAPPING_TYPE_EXPORT</code> or null.
     * @return Collection of mapped templates
     */
    public static Table<MappingType, String, Set<Map<String, String>>> getMappedTemplates(final Resource resource,
                                                                                          final String side) {
        Table<MappingType, String, Set<Map<String, String>>> mappedTemplatesAndItems = HashBasedTable.create();
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(resource);
        String sidePredicate = getSidePredicate(side);
        String query = String.format(NON_EMPTY_MAPPING_QUERY, containingPage.getPath(), sidePredicate);
        Iterator<Resource> mappingResources = resourceResolver.findResources(query, Query.JCR_SQL2);
        while (mappingResources.hasNext()) {
            Resource mappingResource = mappingResources.next();
            String mappingPath = mappingResource.getPath();
            ValueMap valueMap = mappingResource.getValueMap();
            String templateId = valueMap.get(Constants.GC_TEMPLATE_ID_PN, String.class);
            String templateName = valueMap.get(Constants.GC_TEMPLATE_NAME_PN, String.class);
            String mappingName = valueMap.get(Constants.MAPPING_NAME_PN, String.class);
            String importPath = valueMap.get(Constants.AEM_IMPORT_PATH_PN, String.class);
            MappingType mappingType = MappingType.of(valueMap.get(Constants.MAPPING_TYPE_PN, String.class));
            mappingType = mappingType != null ? mappingType : MappingType.TEMPLATE;
            if (templateId != null && templateName != null) {
                Map<String, String> properties = new HashMap<>();
                properties.put(Constants.GC_TEMPLATE_NAME_PN, templateName);
                properties.put(Constants.MAPPING_NAME_PN, mappingName);
                properties.put(Constants.MAPPING_PATH_PARAM_NAME, mappingPath);
                properties.put(Constants.AEM_IMPORT_PATH_PN, importPath);
                Set<Map<String, String>> templateMappingsById = mappedTemplatesAndItems.get(mappingType, templateId);
                //if this is the first mapping for this templateId
                if (templateMappingsById == null) {
                    templateMappingsById = new HashSet<>();
                    mappedTemplatesAndItems.put(mappingType, templateId, templateMappingsById);
                    //if we already have mapping(s) for this templateId
                }
                templateMappingsById.add(properties);
            }
        }
        return mappedTemplatesAndItems;
    }

    /**
     * Receive collection of mappings for project.
     *
     * @param resource  Resource of current cloudservice configuration or any of its children resources.
     * @param projectId ID of project to search in.
     * @param isExport  True for export.
     * @return Map of mapped templates
     */
    public static Map<String, Map<String, String>> getProjectMappings(final Resource resource, final int projectId,
                                                                      final boolean isExport) {
        Map<String, Map<String, String>> mappings = new HashMap<>();
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(resource);
        String query = String.format(MAPPINGS_BY_PROJECTID_QUERY, containingPage.getPath(), projectId,
                isExport ? EXPORT_MAPPING_QUERY_PREDICATE : IMPORT_MAPPING_QUERY_PREDICATE);
        Iterator<Resource> mappingResources = resourceResolver.findResources(query, Query.JCR_SQL2);
        while (mappingResources.hasNext()) {
            Resource mapping = mappingResources.next();
            ValueMap valueMap = mapping.adaptTo(ValueMap.class);
            Map<String, String> properties = new HashMap<>();
            String mappingName = valueMap.get(Constants.MAPPING_NAME_PN, String.class);
            properties.put(Constants.MAPPING_NAME_PN, mappingName);
            properties.put(Constants.MAPPING_MAPPER_STR, valueMap.get(Constants.MAPPING_MAPPER_STR, String.class));
            properties.put(Constants.GC_TEMPLATE_ID_PN, valueMap.get(Constants.GC_TEMPLATE_ID_PN, String.class));
            properties.put(Constants.GC_TEMPLATE_NAME_PN, valueMap.get(Constants.GC_TEMPLATE_NAME_PN, String.class));
            properties.put(Constants.GC_MAPPING_PATH, mapping.getPath());
            mappings.put(mappingName, properties);
        }
        return mappings;
    }

    /**
     * Receive <code>Set</code> of mapped project IDs in current cloudservice configuration.
     *
     * @param resource Resource of current cloudservice configuration or any of its children resources.
     * @param side     <code>Constants.MAPPING_TYPE_IMPORT</code>, <code>Constants.MAPPING_TYPE_EXPORT</code> or null.
     * @return Set of mapped project IDs
     */
    public static Set<String> getMappedProjectsIds(final Resource resource, final String side) {
        ImmutableSet.Builder<String> mappedProjectsIds = ImmutableSet.builder();
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(resource);
        String sidePredicate = getSidePredicate(side);
        String query = String.format(NON_EMPTY_MAPPING_QUERY, containingPage.getPath(), sidePredicate);
        Iterator<Resource> mappingResources = resourceResolver.findResources(query, Query.JCR_SQL2);
        while (mappingResources.hasNext()) {
            Resource mappingResource = mappingResources.next();
            ValueMap valueMap = mappingResource.getValueMap();
            String templateId = valueMap.get(Constants.GC_PROJECT_ID_PN, String.class);
            if (StringUtils.isNotEmpty(templateId)) {
                mappedProjectsIds.add(templateId);
            }
        }
        return mappedProjectsIds.build();
    }

    private static String getSidePredicate(String side) {
        if (side == null) {
            //! Log
            return StringUtils.EMPTY;
        }
        return StringUtils.defaultString(SIDE_PREDICATES.get(side));
    }

    /**
     * Create a valid label out of an arbitrary string using hyphen as replacement for the characters.
     *
     * @param name Name to convert.
     * @return a valid name string.
     */
    public static String createValidName(final String name) {
        return JcrUtil.createValidName(name, JcrUtil.HYPHEN_LABEL_CHAR_MAPPING);
    }

    /**
     * Receive mapped project ID from selector and fill list of mapped projects.
     *
     * @param gcContext    <code>{@link GCContext}</code> object.
     * @param gcContentApi <code>{@link GCContentApi}</code> service.
     * @param accountId    account ID selected in current configuration.
     * @param request      <code>{@link SlingHttpServletRequest}</code> object.
     * @param listProjects list of mapped projects to fill.
     * @param side         <code>Constants.MAPPING_TYPE_IMPORT</code>,
     *                     <code>Constants.MAPPING_TYPE_EXPORT</code> or null.
     * @return mapped project ID.
     * @throws GCException If error occurred during receiving of list of projects from GatherContent
     */
    public static String getMappedProjectIdFromSelector(final GCContext gcContext, final GCContentApi gcContentApi,
                                                        final String accountId, final SlingHttpServletRequest request,
                                                        final Collection<GCProject> listProjects, final String side)
            throws GCException {
        if (accountId == null) {
            //! Log
            return null;
        }
        //at first we are looking for projectId in selectors
        String[] selectors = request.getRequestPathInfo().getSelectors();
        String projectIdFromSelector = findSelector(selectors);
        //if projectId from selectors == one of available projects for this accountId
        //then we use projectId from selectors
        List<GCProject> projects = gcContentApi.projects(gcContext, accountId);
        Set<String> mappedProjectsIds = getMappedProjectsIds(request.getResource(), side);
        for (GCProject gcProject : projects) {
            if (mappedProjectsIds.contains(gcProject.getId())) {
                listProjects.add(gcProject);
            }
        }
        if (projectIdFromSelector != null) {
            for (GCProject project : listProjects) {
                if (projectIdFromSelector.equals(project.getId())) {
                    return projectIdFromSelector;
                }
            }
        }
        return null;
    }

    private static String findSelector(String[] selectors) {
        for (String selector : selectors) {
            if (selector.startsWith(Constants.PROJECT_ID_SELECTOR)) {
                return selector.substring(Constants.PROJECT_ID_SELECTOR.length());
            }
        }
        return null;
    }

    /**
     * Add JCR properties to page to mark that this page was imported from GatherContent to AEN.
     *
     * @param resourceResolver   JCR ResourceResolver.
     * @param modifiableValueMap ModifiableValueMap of AEM page needs to be updated.
     * @param projectId          GatherContent Project ID that will be associated with AEM page.
     * @param itemId             GatherContent Item ID that will be associated with AEM page.
     * @param mappingPath        Path to mapping that will be associated with AEM page.
     * @throws PersistenceException If any error occurs during saving changes to JCR Repository
     */
    public static void addGCProperties(final ResourceResolver resourceResolver,
                                       final ModifiableValueMap modifiableValueMap, final String projectId,
                                       final String itemId, final String mappingPath) throws PersistenceException {
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_MARKER, true);
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_PROJECT_ID, projectId);
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_ITEM_ID, itemId);
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_MAPPING_PATH, mappingPath);
        resourceResolver.commit();
    }

    /**
     * Add JCR properties to page to mark that this page was exported from AEM to GatherContent.
     *
     * @param resourceResolver   JCR ResourceResolver.
     * @param modifiableValueMap ModifiableValueMap of AEM page needs to be updated.
     * @param projectId          GatherContent Project ID that will be associated with AEM page.
     * @param itemId             GatherContent Item ID that will be associated with AEM page.
     * @param mappingPath        Path to mapping that will be associated with AEM page.
     * @throws PersistenceException If any error occurs during saving changes to JCR Repository
     */
    public static void addGCExportProperties(final ResourceResolver resourceResolver,
                                             final ModifiableValueMap modifiableValueMap, final String projectId,
                                             final String itemId, final String mappingPath) throws PersistenceException {
        modifiableValueMap.put(Constants.GC_EXPORTED_PAGE_MARKER, true);
        modifiableValueMap.put(Constants.GC_EXPORTED_PAGE_PROJECT_ID, projectId);
        modifiableValueMap.put(Constants.GC_EXPORTED_PAGE_ITEM_ID, itemId);
        modifiableValueMap.put(Constants.GC_EXPORTED_PAGE_MAPPING_PATH, mappingPath);
        resourceResolver.commit();
    }

    /**
     * Prepare string value form GatherContent for writing into AEM JCR repository.
     * When we get value from GatherContent we perform double HTML unescape.
     *
     * @param value String value from GatherContent
     * @return Prepared string value to write into AEM JCR repository.
     */
    public static String unescapeGCString(String value) {
        return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(value));
    }

    /**
     * Get hierarchically looking name according to GatherContent structure.
     *
     * @param itemList List of Items with information about GatherContent parent-child relationship.
     * @param parentId GatherContent ID of parent item.
     * @param name     Initial name.
     * @return Hierarchically looking name.
     */
    public static String getHierarchyName(final List<? extends GCHierarchySortable> itemList, final String parentId,
                                          final String name) {
        if (parentId == null || name == null) {
            return name;
        }
        String result = name;
        for (GCHierarchySortable item : itemList) {
            if (parentId.equals(item.getId())) {
                result = getHierarchyName(itemList, item.getParentId(), Constants.NEXT_LEVEL_HIERARCHY_INDENT + result);
                break;
            }
        }
        return result;
    }

    /**
     * Get string representation of job type based on flags.
     *
     * @param isUpdate     True if this is 'Update' job, false otherwise.
     * @param isImportInGC True if this is 'Export' job, false if this is 'Import' job.
     * @return Job type.
     */
    public static String getJobType(final Boolean isUpdate, final Boolean isImportInGC) {
        StringBuilder jobType = new StringBuilder();
        jobType.append(isImportInGC ? Constants.JOB_TYPE_EXPORT : Constants.JOB_TYPE_IMPORT);
        if (isUpdate) {
            jobType.append(Constants.JOB_TYPE_POSTFIX_UPDATE);
        }
        return jobType.toString();
    }

    /**
     * Reorder List and put children items after parent items.
     *
     * @param gcItems List with items which have GatherContent ID and parent ID.
     * @param <T>     The type of object in the list.
     * @return Reordered list of items.
     */
    public static <T extends GCHierarchySortable> List<T> reorderGcChildren(Collection<T> gcItems) {
        Map<String, TreeNode<T>> trees = new TreeMap<>();

        for (T item : gcItems) {
            TreeNode<T> itemTreeNode = new TreeNode<>(item, item.getId(), item.getParentId());
            if (trees.containsKey(itemTreeNode.getId())) {
                trees.get(itemTreeNode.getId()).addData(item);
            } else {
                trees.put(itemTreeNode.getId(), itemTreeNode);
            }
        }

        for (TreeNode<T> treeNode : trees.values()) {
            if (trees.containsKey(treeNode.getParentId())) {
                TreeNode<T> parent = trees.get(treeNode.getParentId());
                parent.addChild(treeNode);
                treeNode.setParent(parent);
            }
        }

        for (Iterator<Map.Entry<String, TreeNode<T>>> it = trees.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, TreeNode<T>> entry = it.next();
            if (!entry.getValue().isRoot()) {
                it.remove();
            }
        }

        List<T> reordered = new ArrayList<>(gcItems.size());

        for (TreeNode<T> root : trees.values()) {
            TreeNode.treeToList(reordered, root);
        }

        return reordered;
    }

    /**
     * Create SimpleDateFormat instance with timezone.
     *
     * @param gcTime           Date instance of <code>GCTime</code> type.
     * @param outputDateFormat Desired format of SimpleDateFormat.
     * @return SimpleDateFormat instance.
     */
    public static SimpleDateFormat getOutputSimpleDateFormatWithTimeZone(GCTime gcTime, String outputDateFormat) {
        SimpleDateFormat outputSimpleDateFormat = new SimpleDateFormat(outputDateFormat);
        if (gcTime != null && gcTime.getTimezone() != null) {
            outputSimpleDateFormat.setTimeZone(TimeZone.getTimeZone(gcTime.getTimezone()));
        }
        return outputSimpleDateFormat;
    }

    /**
     * Convert Date from GC to <code>Constants.ITEM_DATE_FORMAT</code> format.
     *
     * @param gcTime Date instance of <code>GCTime</code> type.
     * @return Date instance of java.util.Date type.
     */
    public static Date getDateFromGCItem(GCTime gcTime) {
        if (gcTime == null || gcTime.getDate() == null) {
            //! Log
            return null;
        }
        SimpleDateFormat itemDateFormat = new SimpleDateFormat(Constants.ITEM_DATE_FORMAT);
        try {
            return itemDateFormat.parse(gcTime.getDate());
        } catch (ParseException e) {
            LOGGER.error("Item date parse failed", e);
            return null;
        }
    }
}
