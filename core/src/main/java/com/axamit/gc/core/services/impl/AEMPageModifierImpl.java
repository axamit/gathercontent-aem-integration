/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.impl;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.*;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.api.services.GCContentNewApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.FieldMappingProperties;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;
import com.axamit.gc.core.services.AbstractPageModifier;
import com.axamit.gc.core.services.FailSafeExecutor;
import com.axamit.gc.core.services.plugins.GCPluginManager;
import com.axamit.gc.core.services.AEMPageModifier;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.axamit.gc.core.sightly.models.MapperModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.ResourceResolverUtil;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.*;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OSGI service implements <code>{@link AEMPageModifier}</code> interface which provides methods to create pages, assets
 * and provide field mapping information, which also needs access to JCR repository in AEM.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = AEMPageModifier.class)
@Component(description = "AEM Page Modifier Service", name = "AEM Page Modifier", immediate = true, metatype = true)
public final class AEMPageModifierImpl extends AbstractPageModifier implements AEMPageModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(AEMPageModifierImpl.class);
    private static final int MAX_FIELD_LENGTH_TO_SHOW = 60;

    private static final String ALL_PAGES_WITH_TEMPLATE_QUERY =
            "SELECT * FROM [cq:PageContent] AS pageContent WHERE ISDESCENDANTNODE(pageContent , '%s')"
                    + " AND [cq:template]='%s'";
    private static final String DEFAULT_ABSTRACT_TEMPLATE_LIMIT_PATH = "/content";

    @Reference
    private MimeTypeService mimeTypeService;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private GCContentApi gcContentApi;

    @Reference
    private GCContentNewApi gcContentNewApi;

    @Reference
    private GCPluginManager gcPluginManager;

    @Reference
    private FailSafeExecutor failSafeExecutor;

    /**
     * @inheritDoc
     */
    @Override
    public ImportResultItem updatePage(final GCContext gcContext, final ImportItem importItem) {
        LOGGER.debug("Create/update item {}", importItem.getItemId());
        ResourceResolver resourceResolver = null;
        //first need to create assets
        try {
            resourceResolver = getPageCreatorResourceResolver();
            final GCItem gcItem = gcContentNewApi.itemById(gcContext, importItem.getItemId());
            final GCTemplate gcTemplate = gcContentNewApi.template(gcContext, gcItem.getTemplateId());
            final List<GCStatus> statusesByProjectId = gcContentApi.statusesByProjectId(gcContext, gcItem.getProjectId());
            final Resource mappingResource = resourceResolver.resolve(importItem.getMappingPath());
            final MapperModel mapperModel = adaptMapperModel(mappingResource);

            String importDAMPath = Constants.DEFAULT_IMPORT_DAM_PATH;
            Map<String, FieldMappingProperties> mapping = Collections.emptyMap();
            Map<String, String> metaMapping = Collections.emptyMap();
            String pluginConfigPath = StringUtils.EMPTY;
            String mappingName = StringUtils.EMPTY;

            if (mapperModel != null) {
                importDAMPath = updateImportDAMPath(importDAMPath, mapperModel);
                mapping = mapperModel.getMapper();
                metaMapping = mapperModel.getMetaMapper();
                pluginConfigPath = mapperModel.getPluginConfigPath();
                mappingName = mapperModel.getMappingName();
            }
            if (StringUtils.isNotBlank(importItem.getImportPath())) {
                LOGGER.debug("Imported/updated item {}", importItem.getItemId());
                return createUpdatePage(gcContext, resourceResolver, gcItem, gcTemplate, importItem, importDAMPath, mapping,
                        metaMapping, pluginConfigPath, mappingName);
            } else {
                LOGGER.error("Import/update item {} failed, AEM import path is blank", importItem.getItemId());
                final GCStatus gcStatus = statusesByProjectId.stream()
                        .filter(status -> status.getId().equals(gcItem.getStatusId()))
                        .findAny()
                        .orElse(null);
                return new ImportResultItem(
                        gcStatus != null ? gcStatus.getDisplayName() : null,
                        gcItem.getName(),
                        null,
                        ImportResultItem.NOT_IMPORTED,
                        importItem.getTemplate(),
                        null,
                        null,
                        gcStatus != null ? gcStatus.getColor() : null,
                        gcItem.getPosition(),
                        gcItem.getId(),
                        gcItem.getFolderUuid(),
                        importItem.getImportIndex(),
                        null);
            }
        } catch (LoginException e) {
            LOGGER.error("Failed to get ServiceResourceResolver {}", e.getMessage());
        } catch (GCException e) {
            LOGGER.error("Failed to get get data from GC {}", e.getMessage());
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        LOGGER.debug("Couldn't import/update item {}", importItem.getItemId());
        return new ImportResultItem()
                .setImportStatus(ImportResultItem.NOT_IMPORTED)
                .setGcTemplateName(importItem.getTemplate());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Page createPage(final ImportItem importItem, final Map<String, Integer> mapPageCount) {
        ResourceResolver resourceResolver = null;
        //first need to create assets
        try {
            resourceResolver = getPageCreatorResourceResolver();
            final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            String templatePath;
            if (StringUtils.isNotBlank(importItem.getMappingPath())) {
                templatePath = resolveTemplatePath(resourceResolver, importItem.getMappingPath());
            } else {
                templatePath = Constants.NO_TEMPLATE_PAGE_PATH;
            }

            if (StringUtils.isNotBlank(templatePath)) {
                final Page source = pageManager.getPage(templatePath);
                if (source == null) {
                    LOGGER.error("No AEM template page was found at {}. Can not import", templatePath);
                    return null;
                }
                String importPath = importItem.getImportPath();
                if (StringUtils.isBlank(importPath)) {
                    importPath = Constants.DEFAULT_IMPORT_PATH;
                }

                if (createDefaultImportPath(pageManager, importPath)) {
                    return null;
                }
                //fix destination for siblings
                String destination = importPath.endsWith("/")
                        ? (importPath + GCUtil.createValidName(importItem.getTitle()))
                        : GCStringUtil.appendNewLevelToPath(importPath, GCUtil.createValidName(importItem.getTitle()));
                final String destinationPure = destination;
                int alreadyImported = mapPageCount.get(destination) == null ? 0 : mapPageCount.get(destination);
                destination = (alreadyImported > 0) ? (destination + (alreadyImported - 1)) : destination;
                Page targetPage = pageManager.getPage(destination);
                if (targetPage == null) {
                    targetPage = pageManager.copy(source, destination, null, true, true, true);
                } else if (!StringUtils.equals(source.getPath(), targetPage.getPath())) {
                    resourceResolver.delete(targetPage.getContentResource());
                    pageManager.copy(source.getContentResource(), GCStringUtil.appendNewLevelToPath(destination, NameConstants.NN_CONTENT),
                            null, false, true, true);
                }
                mapPageCount.put(destinationPure, alreadyImported + 1);
                return targetPage;
            }
        } catch (LoginException e) {
            LOGGER.error("Failed to get ServiceResourceResolver {}", e.getMessage());
        } catch (PersistenceException | WCMException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        return null;
    }

    @Override
    public Map<String, Map<String, String>> getFieldsMappings(final List<GCTemplateField> gcFields,
                                                              final boolean useAbstract,
                                                              final boolean addEmptyValue,
                                                              final String templatePath,
                                                              final String configurationPath,
                                                              String abstractTemplateLimitPath)
            throws LoginException, RepositoryException {
        if (StringUtils.isEmpty(abstractTemplateLimitPath)) {
            abstractTemplateLimitPath = DEFAULT_ABSTRACT_TEMPLATE_LIMIT_PATH;
        }
        ResourceResolver resourceResolver = null;
        Map<String, Map<String, String>> fieldsMappings = new HashMap<>();
        try {
            resourceResolver = getPageCreatorResourceResolver();

            List<Page> allTemplatePages = getAllTemplatePages(useAbstract, templatePath, resourceResolver, abstractTemplateLimitPath);

            Map<Property, String> allPropertiesMap = new HashMap<>();

            getAllPropertiesMap(allTemplatePages, allPropertiesMap);

            for (GCTemplateField gcField : gcFields) {
                Map<String, String> fieldMapping =
                        getFieldMapping(resourceResolver, addEmptyValue, allPropertiesMap, gcField.getType(),
                                gcField.getLabel(), configurationPath);
                fieldsMappings.put(gcField.getUuid(), fieldMapping);
            }

            Map<String, String> fieldMapping = getFieldMapping(resourceResolver, addEmptyValue, allPropertiesMap,
                    GCElementType.TEXT, "", configurationPath);
            Map<String, String> metaNameFieldMapping = new LinkedHashMap<>();
            String jcrTitleValue = fieldMapping.get(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE);
            jcrTitleValue = ((jcrTitleValue != null) && !jcrTitleValue.isEmpty())
                    ? jcrTitleValue : (JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE);
            metaNameFieldMapping.put(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE, jcrTitleValue);
            metaNameFieldMapping.putAll(fieldMapping);
            fieldsMappings.put(Constants.META_ITEM_NAME, metaNameFieldMapping);
            return fieldsMappings;
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

    /**
     * Creates a new Asset at the given path in AEM JCR repository. Remote URL is used as a source.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param parentPath The path of the asset to be created.
     * @param gcFile     JSON data of source.
     * @param mimeType   The mime type of the new asset's original binary.
     * @param doSave     Whether the repository changes are saved or not.
     * @return The newly created asset.
     */
    public Asset createAsset(final GCContext gcContext, final String parentPath, final GCFile gcFile, final String mimeType,
                             final boolean doSave) {
        ResourceResolver resourceResolver = null;
        Asset result = null;
        try {
            resourceResolver = getPageCreatorResourceResolver();
            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(gcFile.getUrl()).openConnection();
            setAuthorizationAndHeaders(gcContext, urlConnection);
            String mimeTypeForAsset;
            if (StringUtils.isEmpty(mimeType)) {
                mimeTypeForAsset = mimeTypeService.getMimeType(parentPath);
                if (StringUtils.isEmpty(mimeTypeForAsset)) {
                    mimeTypeForAsset = urlConnection.getContentType();
                }
            } else {
                mimeTypeForAsset = mimeType;
            }

            try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
                result = assetManager.createAsset(parentPath, in, mimeTypeForAsset, doSave);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        return result;
    }

    @SuppressWarnings("checkstyle:parameternumber")
    private ImportResultItem createUpdatePage(final GCContext gcContext, final ResourceResolver resourceResolver,
                                              final GCItem gcItem, final GCTemplate gcTemplate, final ImportItem importItem,
                                              final String importDAMPath, final Map<String, FieldMappingProperties> mapping,
                                              final Map<String, String> metaMapping, final String configurationPath,
                                              final String mappingName) {
        try {
            Page targetPage = failSafeExecutor.executeWithRetries(() -> {
                final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                final Page resultingPage = pageManager.getPage(importItem.getImportPath());
                pageManager.createRevision(resultingPage);
                final Map<String, Asset> gcAssets = uploadFilesToAssets(gcContext, gcItem, mapping, importDAMPath);
                final ModifiableValueMap modifiableValueMap = resultingPage != null ? resultingPage.getContentResource().adaptTo(ModifiableValueMap.class) : null;
                GCUtil.addGCProperties(resourceResolver,
                        modifiableValueMap,
                        gcItem.getProjectId(),
                        gcItem.getId(),
                        importItem.getMappingPath() != null
                                ? importItem.getMappingPath()
                                : StringUtils.EMPTY
                );
                modifiableValueMap.put(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());

                //update properties based on template
                final Collection<String> updatedProperties = new HashSet<>();
                updateMetaProperties(gcItem, metaMapping, resultingPage, updatedProperties);
                updateProperties(resourceResolver, gcItem, gcTemplate, mapping, configurationPath, resultingPage, gcAssets, updatedProperties);
                resourceResolver.commit();
                return resultingPage;
            });

            Boolean updateItemStatus = false;
            if (importItem.getNewStatusData().getId() != null) {
                updateItemStatus = gcContentApi.updateItemStatus(gcContext, gcItem.getId(), importItem.getNewStatusData().getId());
            }

            GCStatus statusData;
            if (Boolean.TRUE.equals(updateItemStatus)) {
                statusData = importItem.getNewStatusData();
            } else {
                final List<GCStatus> statusesByProjectId = gcContentApi.statusesByProjectId(gcContext, gcItem.getProjectId());
                statusData = statusesByProjectId.stream()
                        .filter(status -> status.getId().equals(gcItem.getStatusId()))
                        .findAny()
                        .orElse(null);
                if (statusData == null) {
                    throw new GCException("Can't get status");
                }
            }
            return new ImportResultItem(
                    statusData.getDisplayName(),
                    gcItem.getName(),
                    targetPage.getTitle(),
                    ImportResultItem.IMPORTED,
                    importItem.getTemplate(),
                    "https://" + importItem.getSlug() + ".gathercontent.com/item/" + gcItem.getId(),
                    targetPage.getPath(),
                    statusData.getColor(),
                    gcItem.getPosition(),
                    gcItem.getId(),
                    gcItem.getFolderUuid(),
                    importItem.getImportIndex(),
                    mappingName);
        } catch (GCException e) {
            LOGGER.debug("Couldn't createUpdatePage item {}: {}", importItem.getItemId(), e);
            return new ImportResultItem().setImportStatus(ImportResultItem.NOT_IMPORTED)
                    .setGcTemplateName(importItem.getTemplate());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ImportResultItem(
                    null,
                    gcItem.getName(),
                    null,
                    ImportResultItem.NOT_IMPORTED,
                    importItem.getTemplate(),
                    null,
                    null,
                    null,
                    gcItem.getPosition(),
                    gcItem.getId(),
                    gcItem.getFolderUuid(),
                    importItem.getImportIndex(),
                    mappingName);
        }
    }

    private void updateProperties(ResourceResolver resourceResolver, GCItem gcItem, GCTemplate gcTemplate, Map<String, FieldMappingProperties> mapping,
                                  String configurationPath, Page targetPage, Map<String, Asset> gcAssets,
                                  Collection<String> updatedProperties) {
        if (mapping != null && configurationPath != null) {
            mapping.forEach((key, value) -> {
                if (!value.getPath().isEmpty()) {
                    GCContent gcContent = findContentByKey(gcItem, key);
                    final GCTemplateField gcTemplateField = findTemplateFieldByKey(gcTemplate, key);
                    value.getPath().stream().filter(propertyPath -> !ResourceUtil.isNonExistingResource(resourceResolver.resolve(GCStringUtil.appendNewLevelToPath(targetPage.getPath(), propertyPath)))).findFirst().ifPresent(propertyPath -> updateProperty(gcContent, gcTemplateField, targetPage, propertyPath, gcAssets, updatedProperties,
                            resourceResolver, configurationPath, value.getPlugin()));
                }
            });
        }
    }

    private void updateMetaProperties(GCItem gcItem, Map<String, String> metaMapping, Page targetPage, Collection<String> updatedProperties) {
        if (metaMapping != null) {
            metaMapping.forEach((key, value) -> {
                if (!value.isEmpty()) {
                    updateMetaProperty(gcItem, targetPage, key, value, updatedProperties);
                }
            });
        }
    }

    private void updateMetaProperty(GCItem gcItem, Page page, String key, String propertyPath,
                                    Collection<String> updatedProperties) {
        Node node = page.adaptTo(Node.class);
        try {
            if (Constants.META_ITEM_NAME.equals(key)) {
                setProperty(node, propertyPath, gcItem.getName(), true, updatedProperties);
            }
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void setProperty(final Node node, final String propertyPath, final String value,
                             final boolean needConcatenation, final Collection<String> updatedProperties)
            throws RepositoryException {
        if (node.hasProperty(propertyPath)) {
            String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
            String property = GCStringUtil.getPropertyNameFromPropertyPath(propertyPath);
            Node destinationNode = node.getNode(relativePath);
            //logic for concatenation multiple GatherContent text fields to a single AEM text field
            if (needConcatenation && updatedProperties.contains(propertyPath)) {
                destinationNode.setProperty(property, destinationNode.getProperty(property).getString() + value);
            } else {
                destinationNode.setProperty(property, value);
            }
            updatedProperties.add(propertyPath);
        } else {
            LOGGER.warn("Property '{}' is absent in AEM template. "
                    + "Possibly AEM template was modified after mapping. Please review", propertyPath);
        }
    }

    //We need to update property based on update strategy
    private void updateProperty(final GCContent gcContent, final GCTemplateField gcTemplateField, final Page page, final String propertyPath,
                                final Map<String, Asset> gcAssets, final Collection<String> updatedProperties,
                                final ResourceResolver resourceResolver, final String configurationPath,
                                final String fieldPlugin) {
        if (gcContent != null) {
            GCPlugin gcPlugin = gcPluginManager.getPlugin(resourceResolver, configurationPath, gcContent.getType().getValue(), null, fieldPlugin);
            try {
                if (gcPlugin != null) {
                    gcPlugin.transformFromGCtoAEM(resourceResolver, page, propertyPath, gcContent, gcTemplateField, updatedProperties,
                            gcAssets);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private String updateImportDAMPath(String importDAMPath, MapperModel mapperModel) {
        if (StringUtils.isNotBlank(mapperModel.getImportDAMPath())) {
            importDAMPath = mapperModel.getImportDAMPath();
        }
        return importDAMPath;
    }


    private static void getAllChildrenProperties(final String pagePath, final Map<String, Property> propertyMap, final Node node)
            throws RepositoryException {
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            propertyMap.put(StringUtils.remove(property.getPath(), pagePath + "/"), property);
        }
        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            getAllChildrenProperties(pagePath, propertyMap, nodeIterator.nextNode());
        }
    }

    private String resolveTemplatePath(ResourceResolver resourceResolver, String mappingPath) {
        String mapperModelTemplatePath = null;
        Resource mappingResource = resourceResolver.resolve(mappingPath);
        MapperModel mapperModel = adaptMapperModel(mappingResource);
        if (mapperModel != null) {
            if (mapperModel.getTemplatePath() != null) {
                mapperModelTemplatePath = mapperModel.getTemplatePath();
            } else {
                LOGGER.error("No AEM Template Page property was found in mapping {}. Can not import", mappingPath);
            }
        } else {
            LOGGER.error("No mapping was found at {}. Can not import", mappingPath);
        }
        return mapperModelTemplatePath;
    }

    private MapperModel adaptMapperModel(Resource mappingResource) {
        MapperModel mapperModel = null;
        if (mappingResource != null && !ResourceUtil.isNonExistingResource(mappingResource)) {
            mapperModel = mappingResource.adaptTo(MapperModel.class);
        }
        return mapperModel;
    }

    private boolean createDefaultImportPath(PageManager pageManager, String importPath) {
        if (importPath.startsWith(Constants.DEFAULT_IMPORT_PATH)
                && pageManager.getPage(Constants.DEFAULT_IMPORT_PATH) == null) {
            try {
                pageManager.create("/content", "gathercontent", null, "GATHERCONTENT default parent page");
            } catch (WCMException e) {
                LOGGER.error("Can not create default '/content/gathercontent' path");
                return true;
            }
        }
        return false;
    }

    private Map<String, Asset> uploadFilesToAssets(final GCContext gcContext, final GCItem gcItem, final Map<String, FieldMappingProperties> mapping, final String importDAMPath) {
        List<GCContent> filesContentList = new ArrayList<>();
        gcItem.getContent().forEach((key, entryValue) -> {
            if (GCElementType.COMPONENT == entryValue.getType()) {
                entryValue.getComponent().forEach((key1, value) -> {
                    if (GCElementType.FILES == value.getType() && isFileInMapping(mapping, key1)) {
                        filesContentList.add(value);
                    }
                });
            } else if (GCElementType.FILES == entryValue.getType() && isFileInMapping(mapping, key)) {
                filesContentList.add(entryValue);
            }
        });
        Map<String, Asset> assetMap = new HashMap<>();

        filesContentList.forEach(filesContent -> filesContent.getFiles().forEach(gcFile -> {
            String parentPath = createAssetFolderStructure(gcItem, gcFile, importDAMPath);
            if (StringUtils.isNotEmpty(parentPath)) {
                Asset asset = createAsset(gcContext, parentPath, gcFile, null, true);
                assetMap.put(gcFile.getFileId(), asset);
            }
        }));
        return assetMap;
    }

    private boolean isFileInMapping(final Map<String, FieldMappingProperties> mapping, final String key) {
        if (mapping != null ) {
            return mapping.containsKey(key);
        }
        return false;
    }

    private String createAssetFolderStructure(final GCItem gcItem, final GCFile gcFile, final String importDAMPath) {
        try {
            return failSafeExecutor.executeWithRetries(() -> {
                ResourceResolver resourceResolver = null;
                try {
                    resourceResolver = getPageCreatorResourceResolver();
                    Session session = resourceResolver.adaptTo(Session.class);
                    Node damNode = JcrUtils.getNodeIfExists(DamConstants.MOUNTPOINT_ASSETS, session);
                    if (damNode != null) {
                        String relativePath = importDAMPath.replaceFirst(DamConstants.MOUNTPOINT_ASSETS, "");
                        relativePath = GCStringUtil.stripFirstSlash(relativePath);
                        Node parentNode = JcrUtils.getOrCreateByPath(damNode, relativePath, false,
                                DamConstants.NT_SLING_ORDEREDFOLDER, DamConstants.NT_SLING_ORDEREDFOLDER, false);
                        if (parentNode != null) {
                            String itemTitle = gcItem.getName();
                            Node itemNode = createDamNode(session, parentNode, itemTitle, String.valueOf(gcItem.getId()));
                            String gcFilename = gcFile.getFilename();
                            int pos = gcFilename.lastIndexOf(".");
                            String gcFilenameTitle = pos > 0 ? gcFilename.substring(0, pos) : gcFilename;
                            Node assetNode = createDamNode(session, itemNode, gcFilenameTitle, gcFile.getFileId());
                            session.save();
                            return GCStringUtil.appendNewLevelToPath(assetNode.getPath(), gcFilename);

                        } else {
                            LOGGER.error("Can not get or create importDAMPath: {}", importDAMPath);
                        }
                    } else {
                        LOGGER.error("Can not get: {}", DamConstants.MOUNTPOINT_ASSETS);
                    }
                } finally {
                    if (resourceResolver != null && resourceResolver.isLive()) {
                        resourceResolver.close();
                    }
                }
                return StringUtils.EMPTY;
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    private Node createDamNode(Session session, Node parentNode, String title, String id) throws RepositoryException {
        Node damNode = JcrUtil.createPath(parentNode, id, false,
                DamConstants.NT_SLING_ORDEREDFOLDER, DamConstants.NT_SLING_ORDEREDFOLDER, session, false);
        Node assetNodeContent = JcrUtil.createPath(damNode, JcrConstants.JCR_CONTENT, false,
                JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, session, false);
        String assetNodeContentTitle =
                JcrUtils.getStringProperty(assetNodeContent, JcrConstants.JCR_TITLE, "");
        if (!title.equals(assetNodeContentTitle)) {
            assetNodeContent.setProperty(JcrConstants.JCR_TITLE, title);
        }
        return damNode;
    }


    private void getAllPropertiesMap(List<Page> allTemplatePages, Map<Property, String> allPropertiesMap) {
        Map<String, Property> allPropertiesRelativeToPage = new HashMap<>();
        allTemplatePages.stream().filter(templatePage -> templatePage != null && templatePage.hasContent()).forEach(templatePage -> {
            Node aemTemplateNode = Objects.requireNonNull(templatePage.getContentResource()).adaptTo(Node.class);
            if (aemTemplateNode != null) {
                try {
                    getAllChildrenProperties(templatePage.getPath(), allPropertiesRelativeToPage, aemTemplateNode);
                } catch (RepositoryException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        allPropertiesRelativeToPage.forEach((key, value) -> allPropertiesMap.put(value, key));
    }

    private List<Page> getAllTemplatePages(boolean useAbstract, String templatePath, ResourceResolver resourceResolver, String abstractTemplateLimitPath) {
        List<Page> allTemplatePages = new ArrayList<>();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page sourceAemTemplatePage = pageManager.getPage(templatePath);
        if (useAbstract) {
            if (sourceAemTemplatePage != null && sourceAemTemplatePage.hasContent()) {
                String cqTemplatePath = Objects.requireNonNull(sourceAemTemplatePage.getContentResource()).getValueMap().get(NameConstants.PN_TEMPLATE, String.class);
                if (StringUtils.isNotEmpty(cqTemplatePath)) {
                    Iterator<Resource> abstractTemplatesPagesResources =
                            resourceResolver.findResources(String.format(ALL_PAGES_WITH_TEMPLATE_QUERY, abstractTemplateLimitPath, cqTemplatePath), Query.JCR_SQL2);
                    while (abstractTemplatesPagesResources.hasNext()) {
                        Resource abstractTemplatesPagesResource = ResourceUtil.unwrap(abstractTemplatesPagesResources.next());
                        if (abstractTemplatesPagesResource != null && abstractTemplatesPagesResource.getParent() != null) {
                            Page adaptedPage = abstractTemplatesPagesResource.getParent().adaptTo(Page.class);
                            if (adaptedPage != null) {
                                allTemplatePages.add(adaptedPage);
                            }
                        }
                    }
                } else {
                    allTemplatePages.add(sourceAemTemplatePage);
                }
            }
        } else {
            allTemplatePages.add(sourceAemTemplatePage);
        }
        return allTemplatePages;
    }

    private Map<String, String> getFieldMapping(final ResourceResolver resourceResolver, final boolean addEmptyValue,
                                                final Map<Property, String> allPropertiesMap,
                                                final GCElementType gcFieldType, final String gcElementLabel,
                                                final String configurationPath) {
        Map<String, String> fieldMapping = new TreeMap<>();
        Map<Property, String> filteredPropertiesMap = new HashMap<>();
        if (addEmptyValue) {
            fieldMapping.put("", "Don't map");
        }
        try {
            GCPlugin gcPlugin = gcPluginManager.getPlugin(resourceResolver, configurationPath, gcFieldType.getValue(), gcElementLabel, StringUtils.EMPTY);
            if (gcPlugin != null) {
                Collection<Property> filteredProperties = gcPlugin.filter(resourceResolver, allPropertiesMap.keySet());
                filteredPropertiesMap = filteredProperties.stream().collect(Collectors.toMap(filteredProperty -> filteredProperty, allPropertiesMap::get, (a, b) -> b));
            }
            fillFieldMapping(fieldMapping, filteredPropertiesMap);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return fieldMapping;
    }

    private void fillFieldMapping(final Map<String, String> fieldMapping, final Map<Property, String> filteredProperties) {
        filteredProperties.forEach((filteredProperty, key) -> {
            try {
                String propertyName = filteredProperty.getName();
                String fieldValue;
                if (filteredProperty.isMultiple()) {
                    String[] values = PropertiesUtil.toStringArray(filteredProperty.getValues(), new String[0]);
                    if (values.length > 0) {
                        fieldValue = StringUtils.join(values, " ");
                    } else {
                        fieldValue = propertyName;
                    }
                } else {
                    if (!"jcr:data".equals(filteredProperty.getName())) {
                        fieldValue = PropertiesUtil.toString(filteredProperty.getValue(), propertyName);
                    } else {
                        fieldValue = "File";
                    }
                }
                fieldValue = fieldValue.substring(0, Math.min(fieldValue.length(), MAX_FIELD_LENGTH_TO_SHOW));
                fieldMapping.put(key, "Path: /" + key + " | Current value: " + fieldValue);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private ResourceResolver getPageCreatorResourceResolver() throws LoginException {
        return ResourceResolverUtil.getResourceResolver(resourceResolverFactory,
                Constants.PAGE_CREATOR_SUBSERVICE_NAME);
    }

    private void setAuthorizationAndHeaders(GCContext gcContext, HttpsURLConnection urlConnection) {
        final String userpass = gcContext.getUsername() + ":" + gcContext.getApikey();
        final String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes(StandardCharsets.UTF_8));
        urlConnection.setRequestProperty("Authorization", basicAuth);
        gcContext.getHeaders().forEach(urlConnection::setRequestProperty);
    }
}
