/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCData;
import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.api.dto.GCFile;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.FieldMappingProperties;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;
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
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGI service implements <code>{@link PageCreator}</code> interface which provides methods to create pages, assets
 * and provide field mapping information, which also needs access to JCR repository in AEM.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = PageCreator.class)
@Component(description = "Page Creator Service", name = "Page Creator", immediate = true, metatype = true)
public final class PageCreatorImpl implements PageCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageCreatorImpl.class);
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
    private GCPluginManager gcPluginManager;
    @Reference
    private FailSafeExecutor failSafeExecutor;


    private static GCElement findByKey(final GCItem gcItem, final String key) {
        for (GCConfig config : gcItem.getConfig()) {
            for (GCElement element : config.getElements()) {
                if (key.equals(element.getName())) {
                    return element;
                }
            }
        }
        return null;
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

    /**
     * @inheritDoc
     */
    @Override
    public Asset createAsset(final GCContext gcContext, final String parentPath, final String sourceId, final String mimeType,
                             final boolean doSave) {
        ResourceResolver resourceResolver = null;
        Asset result = null;
        try {
            resourceResolver = getPageCreatorResourceResolver();
            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);

            String hostUrl = gcContext.getApiURL() + "/files/" + sourceId + "/download";
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(hostUrl).openConnection();
            setAuthorizationAndHeaders(gcContext, urlConnection);
            //urlConnection.setConnectTimeout(10000);
            //urlConnection.setReadTimeout(30000);
            String mimeTypeForAsset;
            if (StringUtils.isEmpty(mimeType)) {
                mimeTypeForAsset = mimeTypeService.getMimeType(parentPath);
                if (StringUtils.isEmpty(mimeTypeForAsset)) {
                    mimeTypeForAsset = urlConnection.getContentType();
                }
            } else {
                mimeTypeForAsset = mimeType;
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = assetManager.createAsset(parentPath, in, mimeTypeForAsset, doSave);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        return result;
    }

    private void setAuthorizationAndHeaders(GCContext gcContext, HttpsURLConnection urlConnection) {
        final String userpass = gcContext.getUsername() + ":" + gcContext.getApikey();
        final String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes(StandardCharsets.UTF_8));
        urlConnection.setRequestProperty("Authorization", basicAuth);
        for (Map.Entry<String, String> entry : gcContext.getHeaders().entrySet()) {
            urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ImportResultItem updateGCPage(final GCContext gcContext, final ImportItem importItem) {
        LOGGER.debug("Create/update item {}", importItem.getItemId());
        ResourceResolver resourceResolver = null;
        //first need to create assets
        try {
            resourceResolver = getPageCreatorResourceResolver();
            GCItem gcItem = gcContentApi.itemById(gcContext, importItem.getItemId());
            Resource mappingResource = resourceResolver.resolve(importItem.getMappingPath());
            MapperModel mapperModel = adaptMapperModel(mappingResource);
            String importDAMPath = Constants.DEFAULT_IMPORT_DAM_PATH;
            Map<String, FieldMappingProperties> mapping = null;
            Map<String, String> metaMapping = null;
            String pluginConfigPath = null;
            String mappingName = null;
            if (mapperModel != null) {
                importDAMPath = updateImportDAMPath(importDAMPath, mapperModel);
                mapping = mapperModel.getMapper();
                metaMapping = mapperModel.getMetaMapper();
                pluginConfigPath = mapperModel.getPluginConfigPath();
                mappingName = mapperModel.getMappingName();
            }
            if (StringUtils.isNotBlank(importItem.getImportPath())) {
                LOGGER.debug("Imported/updated item {}", importItem.getItemId());
                return createUpdatePage(gcContext, resourceResolver, gcItem, importItem, importDAMPath, mapping,
                        metaMapping, pluginConfigPath, mappingName);
            } else {
                LOGGER.error("Import/update item {} failed, AEM import path is blank", importItem.getItemId());
                return new ImportResultItem(gcItem.getStatus().getData().getName(), gcItem.getName(), null,
                        ImportResultItem.NOT_IMPORTED, importItem.getTemplate(), null, null,
                        gcItem.getStatus().getData().getColor(), gcItem.getPosition(), gcItem.getId(),
                        gcItem.getParentId(), importItem.getImportIndex(), null);
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
        return new ImportResultItem().setImportStatus(ImportResultItem.NOT_IMPORTED)
                .setGcTemplateName(importItem.getTemplate());
    }

    private String updateImportDAMPath(String importDAMPath, MapperModel mapperModel) {
        if (StringUtils.isNotBlank(mapperModel.getImportDAMPath())) {
            importDAMPath = mapperModel.getImportDAMPath();
        }
        return importDAMPath;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Page createGCPage(final ImportItem importItem, final Map<String, Integer> mapPageCount) {
        ResourceResolver resourceResolver = null;
        //first need to create assets
        try {
            resourceResolver = getPageCreatorResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            String templatePath;
            if (StringUtils.isNotBlank(importItem.getMappingPath())) {
                templatePath = resolveTemplatePath(resourceResolver, importItem.getMappingPath());
            } else {
                templatePath = Constants.NO_TEMPLATE_PAGE_PATH;
            }

            if (StringUtils.isNotBlank(templatePath)) {
                Page source = pageManager.getPage(templatePath);
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
                        ? importPath + GCUtil.createValidName(importItem.getTitle())
                        : GCStringUtil.appendNewLevelToPath(importPath, GCUtil.createValidName(importItem.getTitle()));
                String destinationPure = destination;
                int alreadyImported = mapPageCount.get(destination) == null ? 0 : mapPageCount.get(destination);
                destination = alreadyImported > 0 ? destination + (alreadyImported - 1) : destination;
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

    /**
     * @inheritDoc
     */
    @Override
    public Map<String, List<Asset>> createGCAssets(final GCContext gcContext, final GCItem gcItem, final String importDAMPath)
        throws GCException {
        List<GCFile> files = gcContentApi.filesByItemId(gcContext, gcItem.getId());
        return uploadFiles(gcContext, gcItem, files, importDAMPath);
    }

    private Map<String, List<Asset>> uploadFiles(final GCContext gcContext, final GCItem gcItem, final Iterable<GCFile> files,
                                                 final String importDAMPath) {
        Map<String, List<Asset>> assetMap = new HashMap<>();

        for (GCFile gcFile : files) {

            String parentPath = createAssetFolderStructure(gcItem, gcFile, importDAMPath);
            if (StringUtils.isNotEmpty(parentPath)) {
                Asset asset = createAsset(gcContext, parentPath, gcFile.getId(), null, true);
                List<Asset> assets;
                if (assetMap.containsKey(gcFile.getField())) {
                    assets = assetMap.get(gcFile.getField());
                } else {
                    assets = new ArrayList<>();
                }
                assets.add(asset);
                assetMap.put(gcFile.getField(), assets);
            }
        }
        return assetMap;
    }

    private String createAssetFolderStructure(final GCItem gcItem, final GCFile gcFile, final String importDAMPath) {
        try {
            return failSafeExecutor.executeWithRetries(new Callable<String>() {
                @Override
                public String call() throws Exception {
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
                                Node itemNode = createDamNode(session, parentNode, itemTitle, gcItem.getId());
                                String gcFilename = gcFile.getFilename();
                                int pos = gcFilename.lastIndexOf(".");
                                String gcFilenameTitle = pos > 0 ? gcFilename.substring(0, pos) : gcFilename;
                                Node assetNode = createDamNode(session, itemNode, gcFilenameTitle, gcFile.getId());
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
                }
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

    @SuppressWarnings("checkstyle:parameternumber")
    private ImportResultItem createUpdatePage(final GCContext gcContext, final ResourceResolver resourceResolver,
                                              final GCItem gcItem, final ImportItem importItem,
                                              final String importDAMPath, final Map<String, FieldMappingProperties> mapping,
                                              final Map<String, String> metaMapping, final String configurationPath,
                                              final String mappingName) {
        try {
            Page targetPage = failSafeExecutor.executeWithRetries(new Callable<Page>() {
                @Override
                public Page call() throws Exception {
                    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                    Page targetPage = pageManager.getPage(importItem.getImportPath());
                    pageManager.createRevision(targetPage);
                    Map<String, List<Asset>> gcAssets = createGCAssets(gcContext, gcItem, importDAMPath);
                    ModifiableValueMap modifiableValueMap = targetPage.getContentResource().adaptTo(ModifiableValueMap.class);
                    GCUtil.addGCProperties(resourceResolver, modifiableValueMap, gcItem.getProjectId(), gcItem.getId(),
                            importItem.getMappingPath() != null ? importItem.getMappingPath() : "");
                    modifiableValueMap.put(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());
                    //update properties based on template
                    Collection<String> updatedProperties = new HashSet<>();
                    updateMetaProperties(gcItem, metaMapping, targetPage, updatedProperties);
                    updateProperties(resourceResolver, gcItem, mapping, configurationPath, targetPage, gcAssets, updatedProperties);
                    resourceResolver.commit();
                    return targetPage;
                }
            });

            Boolean updateItemStatus = false;
            if (importItem.getNewStatusData().getId() != null) {
                updateItemStatus =
                        gcContentApi.updateItemStatus(gcContext, gcItem.getId(), importItem.getNewStatusData().getId());
            }
            final GCData statusData = updateItemStatus ? importItem.getNewStatusData() : gcItem.getStatus().getData();
            return new ImportResultItem(statusData.getName(), gcItem.getName(), targetPage.getTitle(),
                    ImportResultItem.IMPORTED, importItem.getTemplate(), "https://" + importItem.getSlug()
                    + ".gathercontent.com/item/" + gcItem.getId(), targetPage.getPath(), statusData.getColor(),
                    gcItem.getPosition(), gcItem.getId(), gcItem.getParentId(), importItem.getImportIndex(),
                    mappingName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ImportResultItem(gcItem.getStatus().getData().getName(), gcItem.getName(), null,
                    ImportResultItem.NOT_IMPORTED, importItem.getTemplate(), null, null,
                    gcItem.getStatus().getData().getColor(), gcItem.getPosition(), gcItem.getId(),
                    gcItem.getParentId(), importItem.getImportIndex(), mappingName);
        }
    }

    private void updateProperties(ResourceResolver resourceResolver, GCItem gcItem, Map<String, FieldMappingProperties> mapping,
                                  String configurationPath, Page targetPage, Map<String, List<Asset>> gcAssets,
                                  Collection<String> updatedProperties) {
        if (mapping != null && configurationPath != null) {
            for (Map.Entry<String, FieldMappingProperties> mapEntry : mapping.entrySet()) {
                if (!mapEntry.getValue().getPath().isEmpty()) {
                    GCElement gcElement = findByKey(gcItem, mapEntry.getKey());
                    for (String propertyPath : mapEntry.getValue().getPath()) {
                        if (!ResourceUtil.isNonExistingResource(resourceResolver.resolve(
                                GCStringUtil.appendNewLevelToPath(targetPage.getPath(), propertyPath)))
                                || mapEntry.getValue().getPlugin().equals(Constants.CAROUSEL_PLUGIN)) {
                            updateProperty(gcElement, targetPage, propertyPath, gcAssets, updatedProperties,
                                    resourceResolver, configurationPath, mapEntry.getValue().getPlugin());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateMetaProperties(GCItem gcItem, Map<String, String> metaMapping, Page targetPage, Collection<String> updatedProperties) {
        if (metaMapping != null) {
            for (Map.Entry<String, String> mapEntry : metaMapping.entrySet()) {
                if (!mapEntry.getValue().isEmpty()) {
                    updateMetaProperty(gcItem, targetPage, mapEntry.getKey(), mapEntry.getValue(), updatedProperties);
                }
            }
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
    private void updateProperty(final GCElement gcElement, final Page page, final String propertyPath,
                                final Map<String, List<Asset>> gcAssets, final Collection<String> updatedProperties,
                                final ResourceResolver resourceResolver, final String configurationPath,
                                final String fieldPlugin) {
        if (gcElement != null) {
            GCPlugin gcPlugin = gcPluginManager.getPlugin(resourceResolver, configurationPath,
                    gcElement.getType().getType(), gcElement.getLabel(), fieldPlugin);
            try {
                if (gcPlugin != null) {
                    gcPlugin.transformFromGCtoAEM(resourceResolver, page, propertyPath, gcElement, updatedProperties,
                            gcAssets);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Map<String, Map<String, String>> getFieldsMappings(final List<GCConfig> gcConfigs,
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

            for (GCConfig gcConfig : gcConfigs) {
                for (GCElement gcElement : gcConfig.getElements()) {
                    Map<String, String> fieldMapping =
                            getFieldMapping(resourceResolver, addEmptyValue, allPropertiesMap, gcElement.getType(),
                                    gcElement.getLabel(), configurationPath);
                    fieldsMappings.put(gcElement.getName(), fieldMapping);
                }
            }

            Map<String, String> fieldMapping = getFieldMapping(resourceResolver, addEmptyValue, allPropertiesMap,
                    GCElementType.TEXT, "", configurationPath);
            Map<String, String> metaNameFieldMapping = new LinkedHashMap<>();
            String jcrTitleValue = fieldMapping.get(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE);
            jcrTitleValue = jcrTitleValue != null && !jcrTitleValue.isEmpty()
                    ? jcrTitleValue : JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_TITLE;
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

    private void getAllPropertiesMap(List<Page> allTemplatePages, Map<Property, String> allPropertiesMap) throws RepositoryException {
        Map<String, Property> allPropertiesRelativeToPage = new HashMap<>();
        for (Page templatePage : allTemplatePages) {
            if (templatePage != null && templatePage.hasContent()) {
                Node aemTemplateNode = templatePage.getContentResource().adaptTo(Node.class);
                if (aemTemplateNode != null) {
                    getAllChildrenProperties(templatePage.getPath(), allPropertiesRelativeToPage, aemTemplateNode);
                }
            }
        }

        for (Map.Entry<String, Property> entry : allPropertiesRelativeToPage.entrySet()) {
            allPropertiesMap.put(entry.getValue(), entry.getKey());
        }
    }

    private List<Page> getAllTemplatePages(boolean useAbstract, String templatePath, ResourceResolver resourceResolver, String abstractTemplateLimitPath) {
        List<Page> allTemplatePages = new ArrayList<>();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page sourceAemTemplatePage = pageManager.getPage(templatePath);
        if (useAbstract) {
            if (sourceAemTemplatePage != null && sourceAemTemplatePage.hasContent()) {
                String cqTemplatePath = sourceAemTemplatePage.getContentResource().getValueMap().get(NameConstants.PN_TEMPLATE, String.class);
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
                                                final GCElementType gcElementType, final String gcElementLabel,
                                                final String configurationPath) {
        Map<String, String> fieldMapping = new TreeMap<>();

        Map<Property, String> filteredPropertiesMap = new HashMap<>();
        if (addEmptyValue) {
            fieldMapping.put("", "Don't map");
        }
        try {
            GCPlugin gcPlugin = gcPluginManager.getPlugin(resourceResolver, configurationPath,
                    gcElementType.getType(), gcElementLabel, StringUtils.EMPTY);
            if (gcPlugin != null) {
                Collection<Property> filteredProperties = gcPlugin.filter(resourceResolver, allPropertiesMap.keySet());
                for (Property filteredProperty : filteredProperties) {
                    filteredPropertiesMap.put(filteredProperty, allPropertiesMap.get(filteredProperty));
                }
            }
            fillFieldMapping(fieldMapping, filteredPropertiesMap);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return fieldMapping;
    }

    private void fillFieldMapping(final Map<String, String> fieldMapping,
                                  final Map<Property, String> filteredProperties) throws RepositoryException {
        for (Map.Entry<Property, String> entry : filteredProperties.entrySet()) {
            String key = entry.getValue();
            Property filteredProperty = entry.getKey();
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
                fieldValue = PropertiesUtil.toString(filteredProperty.getValue(), propertyName);
            }
            fieldValue = fieldValue.substring(0, Math.min(fieldValue.length(), MAX_FIELD_LENGTH_TO_SHOW));

            fieldMapping.put(key, fieldValue + " (" + key + ")");
        }
    }

    private ResourceResolver getPageCreatorResourceResolver() throws LoginException {
        return ResourceResolverUtil.getResourceResolver(resourceResolverFactory,
                Constants.PAGE_CREATOR_SUBSERVICE_NAME);
    }
}
