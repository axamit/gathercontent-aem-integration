/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import com.adobe.granite.license.ProductInfo;
import com.adobe.granite.license.ProductInfoService;
import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.*;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportUpdateTableItem;
import com.axamit.gc.core.pojo.LinkedGCPage;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.pojo.helpers.GCHierarchySortable;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Query;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final String AEM_PRODUCT_INFO_NAME = "Adobe Experience Manager";

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
    public static Table<MappingType, Integer, Set<Map<String, String>>> getMappedTemplates(final Resource resource,
                                                                                          final String side) {
        Table<MappingType, Integer, Set<Map<String, String>>> mappedTemplatesAndItems = HashBasedTable.create();
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
            Integer templateId = NumberUtils.toInt(valueMap.get(Constants.GC_TEMPLATE_ID_PN, String.class), 0);
            String templateName = valueMap.get(Constants.GC_TEMPLATE_NAME_PN, String.class);
            String mappingName = valueMap.get(Constants.MAPPING_NAME_PN, String.class);
            String importPath = valueMap.get(Constants.AEM_IMPORT_PATH_PN, String.class);
            MappingType mappingType = MappingType.of(valueMap.get(Constants.MAPPING_TYPE_PN, String.class));
            mappingType = mappingType != null ? mappingType : MappingType.TEMPLATE;
            if (templateId != 0 && templateName != null) {
                Map<String, String> properties = new HashMap<>();
                properties.put(Constants.GC_TEMPLATE_NAME_PN, templateName);
                properties.put(Constants.MAPPING_NAME_PN, mappingName);
                properties.put(Constants.MAPPING_PATH_PARAM_NAME, mappingPath);
                properties.put(Constants.AEM_IMPORT_PATH_PN, StringUtils.defaultIfBlank(importPath, StringUtils.EMPTY));
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
        //TODO empty??
        if (containingPage == null) {
            return mappings;
        }
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
    public static Set<Integer> getMappedProjectsIds(final Resource resource, final String side) {
        ImmutableSet.Builder<Integer> mappedProjectsIds = ImmutableSet.builder();
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(resource);
        String sidePredicate = getSidePredicate(side);
        String query = String.format(NON_EMPTY_MAPPING_QUERY, containingPage.getPath(), sidePredicate);
        Iterator<Resource> mappingResources = resourceResolver.findResources(query, Query.JCR_SQL2);
        while (mappingResources.hasNext()) {
            Resource mappingResource = mappingResources.next();
            ValueMap valueMap = mappingResource.getValueMap();
            final int templateId = NumberUtils.toInt(valueMap.get(Constants.GC_PROJECT_ID_PN, String.class), 0);
            if (templateId != 0) {
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
    public static Integer getMappedProjectIdFromSelector(final GCContext gcContext, final GCContentApi gcContentApi,
                                                        final Integer accountId, final SlingHttpServletRequest request,
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
        Set<Integer> mappedProjectsIds = getMappedProjectsIds(request.getResource(), side);
        for (GCProject gcProject : projects) {
            if (mappedProjectsIds.contains(gcProject.getId())) {
                listProjects.add(gcProject);
            }
        }
        if (projectIdFromSelector != null) {
            for (GCProject project : listProjects) {
                final int projectId = NumberUtils.toInt(projectIdFromSelector, 0);
                if (projectId == project.getId()) {
                    return projectId;
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
                                       final ModifiableValueMap modifiableValueMap, final Integer projectId,
                                       final Integer itemId, final String mappingPath) throws PersistenceException {
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
     * @throws GCException          If any error occurs during serializing of links to exported pages
     * @throws PersistenceException If any error occurs during saving changes to JCR Repository
     */
    public static void addGCExportProperties(final ResourceResolver resourceResolver,
                                             final ModifiableValueMap modifiableValueMap, final Integer projectId,
                                             final Integer itemId, final String mappingPath)
            throws PersistenceException, GCException {

        final Map<Integer, LinkedGCPage> linkedGCPages = getLinkedGCPages(modifiableValueMap,
                Constants.GC_EXPORTED_PAGES_MAP, Constants.GC_EXPORTED_PAGE_PROJECT_ID,
                Constants.GC_EXPORTED_PAGE_ITEM_ID, Constants.GC_EXPORTED_PAGE_MAPPING_PATH);

        final LinkedGCPage newLinkedGCPage = new LinkedGCPage(projectId, itemId, mappingPath);
        linkedGCPages.put(itemId, newLinkedGCPage);

        modifiableValueMap.put(Constants.GC_EXPORTED_PAGES_MAP, JSONUtil.fromObjectToJsonString(linkedGCPages));
        modifiableValueMap.put(Constants.GC_EXPORTED_PAGE_MARKER, true);
        resourceResolver.commit();
    }

    /**
     * Get Map of linked GC items to this AEM page.
     *
     * @param valueMap         ValueMap of AEM page.
     * @param linkedPagesMapPN Map of linked GatherContent pages property name.
     * @param projectIdPN      GatherContent Project ID property name.
     * @param itemIdPN         GatherContent Item ID property name.
     * @param mappingPathPN    Path to mapping property name.
     * @return Map of linked GC items
     */
    public static Map<Integer, LinkedGCPage> getLinkedGCPages(final ValueMap valueMap,
                                                             final String linkedPagesMapPN, final String projectIdPN,
                                                             final String itemIdPN, final String mappingPathPN) {
        final String linkedPagesJson = valueMap.get(linkedPagesMapPN, String.class);

        if (StringUtils.isNotEmpty(linkedPagesJson)) {
            try {
                return JSONUtil.fromJsonToMapObject(linkedPagesJson, Integer.class, LinkedGCPage.class);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            final Map<Integer, LinkedGCPage> linkedGCPageMap = new HashMap<>();
            final int projectId = NumberUtils.toInt(valueMap.get(projectIdPN, String.class), 0);
            final int itemId = NumberUtils.toInt(valueMap.get(itemIdPN, String.class), 0);
            final String mappingPath = valueMap.get(mappingPathPN, String.class);
            if (projectId != 0 && itemId != 0 && StringUtils.isNotEmpty(mappingPath)) {
                linkedGCPageMap.put(itemId, new LinkedGCPage(projectId, itemId, mappingPath));
            }
            return linkedGCPageMap;
        }
        return Collections.emptyMap();
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
                result = getHierarchyName(itemList, item.getFolderUuid(), Constants.NEXT_LEVEL_HIERARCHY_INDENT + result);
                break;
            }
        }
        return result;
    }

    /**
     * Get string representation of job type based on flags.
     *
     * @param isUpdate     True if this is 'Update' job, false otherwise.
     * @param isExportToGC True if this is 'Export' job, false if this is 'Import' job.
     * @return Job type.
     */
    public static String getJobType(final Boolean isUpdate, final Boolean isExportToGC) {
        StringBuilder jobType = new StringBuilder();
        jobType.append(isExportToGC ? Constants.JOB_TYPE_EXPORT : Constants.JOB_TYPE_IMPORT);
        if (isUpdate) {
            jobType.append(Constants.JOB_TYPE_POSTFIX_UPDATE);
        }
        return jobType.toString();
    }

//    /**
//     * Reorder List and put children items after parent items.
//     *
//     * @param gcItems List with items which have GatherContent ID and parent ID.
//     * @param <T>     The type of object in the list.
//     * @return Reordered list of items.
//     */
//    public static <T extends GCHierarchySortable> List<T> reorderGcChildren(Collection<T> gcItems) {
//        Map<Integer, TreeNode<T>> trees = new TreeMap<>();
//
//        for (T item : gcItems) {
//            TreeNode<T> itemTreeNode = new TreeNode<>(item, item.getId(), item.getFolderUuid());
//            if (trees.containsKey(itemTreeNode.getId())) {
//                trees.get(itemTreeNode.getId()).addData(item);
//            } else {
//                trees.put(itemTreeNode.getId(), itemTreeNode);
//            }
//        }
//
//        for (TreeNode<T> treeNode : trees.values()) {
//            if (trees.containsKey(treeNode.getParentId())) {
//                TreeNode<T> parent = trees.get(treeNode.getParentId());
//                parent.addChild(treeNode);
//                treeNode.setParent(parent);
//            }
//        }
//
//        for (Iterator<Map.Entry<String, TreeNode<T>>> it = trees.entrySet().iterator(); it.hasNext(); ) {
//            Map.Entry<String, TreeNode<T>> entry = it.next();
//            if (!entry.getValue().isRoot()) {
//                it.remove();
//            }
//        }
//
//        List<T> reordered = new ArrayList<>(gcItems.size());
//
//        for (TreeNode<T> root : trees.values()) {
//            TreeNode.treeToList(reordered, root);
//        }
//
//        return reordered;
//    }

    /**
     * Create SimpleDateFormat instance with timezone.
     *
     * @param calendar         Calendar instance of time.
     * @param outputDateFormat Desired format of SimpleDateFormat.
     * @return SimpleDateFormat instance.
     */
    public static SimpleDateFormat getOutputSimpleDateFormatWithTimeZone(Calendar calendar, String outputDateFormat) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(outputDateFormat);
        dateFormat.setTimeZone(calendar.getTimeZone());
        return dateFormat;
    }

    public static String getUserAgentInfo(ProductInfoService productInfoService) {
        ProductInfo[] infos = productInfoService.getInfos();
        String productInfoString = StringUtils.EMPTY;
        if (infos != null && infos.length > 0) {
            ProductInfo aemProductInfo = null;
            for (ProductInfo productInfo : infos) {
                if (AEM_PRODUCT_INFO_NAME.equals(productInfo.getName())) {
                    aemProductInfo = productInfo;
                    break;
                }
            }
            if (aemProductInfo == null) {
                aemProductInfo = infos[0];
            }
            productInfoString = "-" + aemProductInfo.getVersion().toString();
        }
        return productInfoString;
    }

    public static String apiCall(final String url, final GCContext gcContext, final String userAgentInfo) throws GCException {
        return apiCall(url, gcContext, null, userAgentInfo, false);
    }

    public static String apiCall(final String url, final GCContext gcContext, final Iterable<NameValuePair> params, final String userAgentInfo) throws GCException {
        return apiCall(url, gcContext, params, userAgentInfo, false);
    }

    public static String apiCall(final String url, final GCContext gcContext, final String userAgentInfo, final boolean newApiCall) throws GCException {
        return apiCall(url, gcContext, null, userAgentInfo, newApiCall);
    }

    public static String apiCall(final String url, final GCContext gcContext, final Iterable<NameValuePair> params, final String userAgentInfo, final boolean newApiCall)
            throws GCException {
        StringBuilder requestUrl = new StringBuilder(gcContext.getApiURL()).append(url);
        if (params != null) {
            requestUrl.append("?").append(URLEncodedUtils.format(params, StandardCharsets.UTF_8));
        }
        HttpUriRequest httpUriRequest = new HttpGet(requestUrl.toString());

        HttpClient httpClient = GCUtil.setHeadersAndAuth(httpUriRequest, gcContext, userAgentInfo, newApiCall);

        try {
            String paramsString = params != null ? URLEncodedUtils.format(params, StandardCharsets.UTF_8) : "no params";
            LOGGER.debug("Requested GatherContent URL " + httpUriRequest.getURI()
                    + System.lineSeparator() + "Request method: " + httpUriRequest.getMethod()
                    + System.lineSeparator() + "Request params: " + paramsString);
            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            if (statusCode == HttpStatus.SC_OK) {
                LOGGER.debug("Requested GatherContent URL: " + httpUriRequest.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse
                        + System.lineSeparator() + "ResponseEntity: " + stringBuilder);
                return stringBuilder.toString();
            } else {
                throw new GCException("Requested GatherContent URL: " + httpUriRequest.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse
                        + System.lineSeparator() + "ResponseEntity: " + stringBuilder);
            }
        } catch (IOException e) {
            LOGGER.error("Request to GatherContent URL: {} failed. {}", httpUriRequest.getURI(), e.getMessage());
            throw new GCException(e);
        }
    }


    public static HttpClient setHeadersAndAuth(final HttpRequest httpUriRequest, final GCContext gcContext, final String userAgentInfo, final boolean newApiCall) {
        final Map<String, String> headers = newApiCall ? gcContext.getNewApiHeaders() : gcContext.getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            Header header = new BasicHeader(entry.getKey(), entry.getValue());
            httpUriRequest.setHeader(header);
        }
        httpUriRequest.addHeader(new BasicHeader(HttpHeaders.USER_AGENT, userAgentInfo));
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        String username = gcContext.getUsername();
        String apiKey = gcContext.getApikey();
        if (username != null && apiKey != null) {
            //this is hack for httpclient 4.3.4
            try {
                Credentials credentials = new UsernamePasswordCredentials(username, apiKey);
                httpUriRequest.addHeader(new BasicScheme().authenticate(credentials, httpUriRequest, null));
            } catch (AuthenticationException e) {
                LOGGER.error(e.getMessage(), e);
            }

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, apiKey));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        return httpClientBuilder.build();
    }

    public static List<GCTemplateField> getFieldsByTemplate(GCTemplate gcTemplate) {
        final List<List<GCTemplateField>> listOfFields = gcTemplate.getRelated().getStructure().getGroups()
                .stream()
                .map(GCTemplateGroup::getFieldsWithChildren)
                .collect(Collectors.toList());
        return listOfFields.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static GCFolder buildFolderTree(List<GCFolder> gcFolders) {
        GCFolder rootFolder = gcFolders.stream()
                .filter(folder -> "project-root".equals(folder.getType()))
                .findAny()
                .orElse(null);
        if (rootFolder == null) {
            return null;
        }

        searchChildrenFoldersAndPut(gcFolders, rootFolder);

        return rootFolder;
    }

    private static void searchChildrenFoldersAndPut(final List<GCFolder> gcFolders, final GCFolder rootFolder) {
        gcFolders.forEach(gcFolder -> {
            if (rootFolder.getUuid().equals(gcFolder.getParentUuid())) {
                if (rootFolder.getFolders() != null) {
                    rootFolder.getFolders().add(gcFolder);
                } else {
                    rootFolder.setFolders(new ArrayList<>(Arrays.asList(gcFolder)));
                }
                searchChildrenFoldersAndPut(gcFolders, gcFolder);
            }
        });
    }

    public static void setHierarchyTitles(final List<GCFolder> gcFolders, final List<ImportUpdateTableItem> items) {
        items.forEach(item ->
                gcFolders.stream()
                        .filter(gcFolder -> gcFolder.getUuid().equals(item.getFolderUuid()))
                        .findAny()
                        .ifPresent(hierarchyFolder -> item.setHierarchyTitle(hierarchyFolder.getName()))
        );
    }
}
