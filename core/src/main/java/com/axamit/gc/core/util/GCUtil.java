/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.services.PageCreator;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <code>GCUtil</code> is an utility class presenting functionality using across whole application
 * to perform operations like getting mapped projects, mappings from repository etc.
 * @author Axamit, gc.support@axamit.com
 */
public final class GCUtil {

    private GCUtil() {
    }

    private static final String NON_EMPTY_MAPPING_QUERY =
            "SELECT * FROM [nt:unstructured] AS mapping WHERE ISDESCENDANTNODE(mapping, '%s')"
                    + " AND [sling:resourceType]='gathercontent/components/content/mapping'"
                    + " AND [templateId] <> ''"
                    + " AND [mapperStr] <> ''";

    /**
     * Build a list with hierarchical tree representations of items, when all 'parent' items have links to 'children'.
     *
     * @param importItemList <code>List</code> to order.
     * @return Ordered <code>List</code> of <code>ImportItem</code> in hierarchical tree manner.
     */
    public static List<ImportItem> reorderToTree(final List<ImportItem> importItemList) {
        List<ImportItem> tree = new ArrayList<>();
        List<String> itemIdsWithParents = new ArrayList<>();

        for (ImportItem importItem : importItemList) {
            //if item have no parentId
            if ("0".equals(importItem.getParentId())) {
                tree.add(importItem);
                itemIdsWithParents.add(importItem.getItemId());
            } else { //if item have parentId
                for (ImportItem parentImportItem : importItemList) {
                    if (importItem.getParentId().equals(parentImportItem.getItemId())) {
                        importItem.setImportPath(parentImportItem.getImportPath() + "/"
                                + GCUtil.createValidName(parentImportItem.getTitle()));
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
     * Receive collection of mapped templates in current cloudservice configuration.
     *
     * @param resource Resource of current cloudservice configuration or any of its children resources.
     * @return Collection of mapped templates
     */
    public static Map<String, Set<Map<String, String>>> getMappedTemplates(final Resource resource) {
        Map<String, Set<Map<String, String>>> mappedTemplates = new HashMap<>();
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(resource);
        Iterator<Resource> mappingResources = resourceResolver.findResources(String.format(NON_EMPTY_MAPPING_QUERY,
                containingPage.getPath()), Query.JCR_SQL2);
        while (mappingResources.hasNext()) {
            Resource mappingResource = mappingResources.next();
            String mappingPath = mappingResource.getPath();
            ValueMap valueMap = mappingResource.getValueMap();
            String templateId = valueMap.get(Constants.GC_TEMPLATE_ID_PN, String.class);
            String templateName = valueMap.get(Constants.GC_TEMPLATE_NAME_PN, String.class);
            String mappingName = valueMap.get(Constants.MAPPING_NAME_PN, String.class);
            String importPath = valueMap.get(Constants.AEM_IMPORT_PATH_PN, String.class);
            if (templateId != null && templateName != null) {
                Map<String, String> properties = new HashMap<>();
                properties.put(Constants.GC_TEMPLATE_NAME_PN, templateName);
                properties.put(Constants.MAPPING_NAME_PN, mappingName);
                properties.put(Constants.MAPPING_PATH_PARAM_NAME, mappingPath);
                properties.put(Constants.AEM_IMPORT_PATH_PN, importPath);
                Set<Map<String, String>> templateMappingsById = mappedTemplates.get(templateId);
                //if this is the first mapping for this templateId
                if (templateMappingsById == null) {
                    templateMappingsById = new HashSet<>();
                    templateMappingsById.add(properties);
                    mappedTemplates.put(templateId, templateMappingsById);
                    //if we already have mapping(s) for this templateId
                } else {
                    templateMappingsById.add(properties);
                }
            }
        }
        return mappedTemplates;
    }

    private static Set<String> getMappedProjectsIds(final Resource resource) {
        Set<String> mappedProjectsIds = new HashSet<>();
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(resource);
        Iterator<Resource> mappingResources = resourceResolver.findResources(String.format(NON_EMPTY_MAPPING_QUERY,
                containingPage.getPath()), Query.JCR_SQL2);
        while (mappingResources.hasNext()) {
            Resource mappingResource = mappingResources.next();
            ValueMap valueMap = mappingResource.getValueMap();
            String templateId = valueMap.get(Constants.GC_PROJECT_ID_PN, String.class);
            if (templateId != null && !templateId.isEmpty()) {
                mappedProjectsIds.add(templateId);
            }
        }
        return mappedProjectsIds;
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
     * @return mapped project ID.
     * @throws GCException If error occurred during receiving of list of projects from GatherContent
     */
    public static String getMappedProjectIdFromSelector(final GCContext gcContext, final GCContentApi gcContentApi,
                                                        final String accountId, final SlingHttpServletRequest request,
                                                        final List<GCProject> listProjects)
            throws GCException {
        String projectId = null;
        String projectIdFromSelector = null;
        //at first we are looking for projectId in selectors
        String[] selectors = request.getRequestPathInfo().getSelectors();
        for (String selector : selectors) {
            if (selector.startsWith(Constants.PROJECT_ID_SELECTOR)) {
                projectIdFromSelector = selector.substring(Constants.PROJECT_ID_SELECTOR.length());
                break;
            }
        }
        if (accountId != null) {
            //if projectId from selectors == one of available projects for this accountId
            //then we use projectId from selectors
            List<GCProject> projects = gcContentApi.projects(gcContext, accountId);
            Set<String> mappedProjectsIds = getMappedProjectsIds(request.getResource());
            for (GCProject gcProject : projects) {
                if (mappedProjectsIds.contains(gcProject.getId())) {
                    listProjects.add(gcProject);
                }
            }
            if (projectIdFromSelector != null) {
                for (GCProject project : listProjects) {
                    if (projectIdFromSelector.equals(project.getId())) {
                        projectId = projectIdFromSelector;
                    }
                }
            }
        }
        return projectId;
    }

    /**
     * Builds collection of GatherContent field and accordingly AEM fields which could be mapped to this GatherContent
     * field.
     *
     * @param gctemplate   GatherContent template object of <code>{@link GCTemplate}</code>.
     * @param pageCreator  <code>{@link PageCreator}</code> service.
     * @param templatePath JCR path in AEM to template page.
     * @return Collection of mapped GatherContent and AEM fields.
     * @throws LoginException      If an error occurs during getting ResourceResolver
     * @throws RepositoryException If any error occurs during access JCR Repository
     */
    public static Map<String, Map<String, String>> getFieldsMappings(final GCTemplate gctemplate,
                                                                     final PageCreator pageCreator,
                                                                     final String templatePath)
            throws LoginException, RepositoryException {
        Map<String, Map<String, String>> fieldsMappings = new HashMap<>();
        for (GCConfig gcConfig : gctemplate.getConfig()) {
            for (GCElement gcElement : gcConfig.getElements()) {
                Map<String, String> fieldMapping =
                        pageCreator.getFieldMapping(gcElement.getType(), templatePath);
                fieldsMappings.put(gcElement.getName(), fieldMapping);
            }
        }
        Map<String, String> fieldMapping = pageCreator.getFieldMapping(GCElementType.TEXT, templatePath);
        Map<String, String> metaNameFieldMapping = new LinkedHashMap<>();
        String jcrTitleValue = fieldMapping.get(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE);
        jcrTitleValue = jcrTitleValue != null && !jcrTitleValue.isEmpty()
                ? jcrTitleValue : JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE;
        metaNameFieldMapping.put(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE, jcrTitleValue);
        metaNameFieldMapping.putAll(fieldMapping);
        fieldsMappings.put(Constants.META_ITEM_NAME, metaNameFieldMapping);
        return fieldsMappings;
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
}
