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
import com.axamit.gc.api.dto.GCOption;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.filters.FieldFilter;
import com.axamit.gc.core.filters.MultipleFieldFilter;
import com.axamit.gc.core.filters.SystemFieldFilter;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;
import com.axamit.gc.core.sightly.models.MapperModel;
import com.axamit.gc.core.util.Constants;
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
import org.apache.commons.lang3.time.StopWatch;
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
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * OSGI service implements <code>{@link PageCreator}</code> interface which provides methods to create pages, assets
 * and provide field mapping information, which also needs access to JCR repository in AEM.
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = PageCreator.class)
@Component(description = "Page Creator Service", name = "Page Creator", immediate = true, metatype = true)
public final class PageCreatorImpl implements PageCreator {

    private static final String PAGE_CREATOR = "PageCreator";
    private static final int MAX_FIELD_LENGTH_TO_SHOW = 60;

    private static final Logger LOGGER = LoggerFactory.getLogger(PageCreatorImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private GCContentApi gcContentApi;

    private static void addGCProperties(final ModifiableValueMap modifiableValueMap, final String projectId,
                                        final String itemId, final String mappingPath) {
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_MARKER, true);
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_PROJECT_ID, projectId);
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_ITEM_ID, itemId);
        modifiableValueMap.put(Constants.GC_IMPORTED_PAGE_MAPPING_PATH, mappingPath);
    }

    private static void setMultipleStringProperty(final Node destinationNode, final String propertyName,
                                                  final List<String> stringValues)
            throws RepositoryException {
        if (!stringValues.isEmpty()) {
            if (destinationNode.hasProperty(propertyName)) {
                Property property = destinationNode.getProperty(propertyName);
                if (property != null) {
                    property.remove();
                }
            }
            if (stringValues.size() > 1) {
                destinationNode.setProperty(propertyName, stringValues.toArray(new String[stringValues.size()]));
            } else {
                destinationNode.setProperty(propertyName, stringValues.get(0));
            }
        }
    }

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

    private static void getAllChildrenProperties(final List<Property> propertyList, final Node node)
            throws RepositoryException {
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            propertyList.add(properties.nextProperty());
        }
        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            getAllChildrenProperties(propertyList, nodeIterator.nextNode());
        }
    }

    private static List<FieldFilter> getFilterListForText() {
        List<FieldFilter> filterList = new ArrayList<>();
        filterList.add(new SystemFieldFilter());
        return filterList;
    }

    private static List<FieldFilter> getFilterListForSelections() {
        List<FieldFilter> filterList = new ArrayList<>();
        filterList.add(new MultipleFieldFilter());
        filterList.add(new SystemFieldFilter());
        return filterList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Asset createAsset(final String parentPath, final String sourceURL, final String mimetype,
                             final boolean doSave) throws LoginException {
        ResourceResolver resourceResolver = null;
        Asset result = null;
        StopWatch sw = new StopWatch();
        sw.start();
        try {
            resourceResolver = getPageCreatorResourceResolver();
            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
            URLConnection urlConnection = new URL(sourceURL).openConnection();
            //urlConnection.setConnectTimeout(10000);
            //urlConnection.setReadTimeout(30000);
            String mimeTypeForAsset = mimetype == null ? urlConnection.getContentType() : mimetype;
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = assetManager.createAsset(parentPath, in, mimeTypeForAsset, doSave);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
            sw.stop();
            LOGGER.debug("Execution time: Downloading and creating of Asset - {} ms", sw.getTime());
        }
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ImportResultItem updateGCPage(final GCContext gcContext, final ImportItem importItem) {
        StopWatch sw = new StopWatch();
        sw.start();
        LOGGER.debug("Create/update item {}", importItem.getItemId());
        ResourceResolver resourceResolver = null;
        //first need to create assets
        try {
            resourceResolver = getPageCreatorResourceResolver();
            GCItem gcItem = gcContentApi.itemById(gcContext, importItem.getItemId());
            Resource mappingResource = resourceResolver.resolve(importItem.getMappingPath());
            if (mappingResource != null) {
                MapperModel mapperModel = mappingResource.adaptTo(MapperModel.class);
                Map<String, String> mapping = mapperModel.getMapper();
                Map<String, String> metaMapping = mapperModel.getMetaMapper();
                String templatePath = mapperModel.getTemplatePath();
                String importDAMPath = mapperModel.getImportDAMPath();
                if (importDAMPath == null || importDAMPath.isEmpty()) {
                    importDAMPath = Constants.DEFAULT_IMPORT_DAM_PATH;
                }
                if (templatePath != null && !templatePath.isEmpty() && importItem.getImportPath() != null
                        && !importItem.getImportPath().isEmpty() && mapping != null) {
                    LOGGER.debug("Imported/updated item {}", importItem.getItemId());
                    return createUpdatePage(gcContext, resourceResolver, gcItem, importItem, importDAMPath, mapping,
                            metaMapping);
                } else {
                    LOGGER.error(
                            "There are problems with mapping to {} page. Some empty fields was found. Can not import",
                            templatePath);
                    return new ImportResultItem(gcItem.getStatus().getData().getName(), gcItem.getName(),
                            ImportResultItem.NOT_IMPORTED, importItem.getTemplate(), null, null,
                            gcItem.getStatus().getData().getColor());
                }
            }
        } catch (LoginException e) {
            LOGGER.error("Failed to get ServiceResourceResolver {}", e.getMessage());
        } catch (GCException e) {
            LOGGER.error("Failed to get get data from GC {}", e.getMessage());
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
            sw.stop();
            LOGGER.debug("Execution time: updateGCPage method - {} ms", sw.getTime());
        }
        LOGGER.debug("Couldn't import/update item {}", importItem.getItemId());
        return new ImportResultItem(null, null, ImportResultItem.NOT_IMPORTED, importItem.getTemplate(),
                null, null, null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Page createGCPage(final ImportItem importItem, final Map<String, Integer> mapPageCount) {
        ResourceResolver resourceResolver = null;
        //first need to create assets
        StopWatch sw = new StopWatch();
        sw.start();
        try {
            resourceResolver = getPageCreatorResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Resource mappingResource = resourceResolver.resolve(importItem.getMappingPath());
            if (mappingResource != null) {
                MapperModel mapperModel = mappingResource.adaptTo(MapperModel.class);
                String templatePath = mapperModel.getTemplatePath();
                if (templatePath != null && !templatePath.isEmpty()) {
                    Page source = pageManager.getPage(templatePath);
                    if (source == null) {
                        LOGGER.error("No AEM template page was found at {}. Can not import", templatePath);
                        return null;
                    }
                    Page targetPage;
                    String pageName = importItem.getTitle();
                    String importPath = importItem.getImportPath();
                    if (importPath == null || importPath.isEmpty()) {
                        importPath = Constants.DEFAULT_IMPORT_PATH;
                    }
                    String destination = importPath.endsWith("/") ? importPath + GCUtil.createValidName(pageName)
                            : importPath + "/" + GCUtil.createValidName(pageName);

                    if (importPath.startsWith(Constants.DEFAULT_IMPORT_PATH)
                            && pageManager.getPage(Constants.DEFAULT_IMPORT_PATH) == null) {
                        try {
                            pageManager.create("/content", "gathercontent", null, "GATHERCONTENT default parent page");
                        } catch (WCMException e) {
                            LOGGER.error("Can not create default '/content/gathercontent' path");
                            return null;
                        }
                    }
                    //fix destination for siblings
                    String destinationPure = destination;
                    int alreadyImported = mapPageCount.get(destination) == null ? 0 : mapPageCount.get(destination);
                    destination = alreadyImported > 0 ? destination + (alreadyImported - 1) : destination;
                    targetPage = pageManager.getPage(destination);
                    if (targetPage == null) {
                        targetPage = pageManager.copy(source, destination, null, true, true, true);
                    } else {
                        resourceResolver.delete(targetPage.getContentResource());
                        pageManager.copy(source.getContentResource(), destination + "/" + NameConstants.NN_CONTENT,
                                null, false, true, true);
                    }
                    mapPageCount.put(destinationPure, alreadyImported + 1);
                    return targetPage;
                }
            }
        } catch (LoginException e) {
            LOGGER.error("Failed to get ServiceResourceResolver {}", e.getMessage());
        } catch (PersistenceException | WCMException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
            sw.stop();
            LOGGER.debug("Execution time: Creating AEM page - {} ms", sw.getTime());
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Map<String, Asset> createGCAssets(final GCContext gcContext, final GCItem gcItem, final String importDAMPath)
            throws RepositoryException, LoginException, GCException {
        List<GCFile> files = gcContentApi.filesByItemId(gcContext, gcItem.getId());
        return uploadFiles(gcItem, files, importDAMPath);
    }

    private Map<String, Asset> uploadFiles(final GCItem gcItem, final Iterable<GCFile> files,
                                           final String importDAMPath) throws LoginException {
        StopWatch sw = new StopWatch();
        sw.start();
        Map<String, Asset> assetMap = new HashMap<>();

        for (GCFile gcFile : files) {

            String parentPath = createAssetFolderStructure(gcItem, gcFile, importDAMPath);
            if (parentPath != null) {
                Asset asset = createAsset(parentPath, gcFile.getUrl(), null, true);
                assetMap.put(gcFile.getField(), asset);
            }
        }
        sw.stop();
        LOGGER.debug("Execution time: Creating {} Assets - {} ms", assetMap.size(), sw.getTime());
        return assetMap;
    }

    private String createAssetFolderStructure(final GCItem gcItem, final GCFile gcFile, final String importDAMPath) {
        String path = null;
        ResourceResolver resourceResolver = null;
        StopWatch sw = new StopWatch();
        sw.start();
        try {
            resourceResolver = getPageCreatorResourceResolver();
            Session session = resourceResolver.adaptTo(Session.class);
            Node damNode = JcrUtils.getNodeIfExists(DamConstants.MOUNTPOINT_ASSETS, session);
            if (damNode != null) {
                String relativePath = importDAMPath.replaceFirst(DamConstants.MOUNTPOINT_ASSETS, "");
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.replaceFirst("/", "");
                }
                Node parentNode = JcrUtils.getOrCreateByPath(damNode, relativePath, false,
                        DamConstants.NT_SLING_ORDEREDFOLDER, DamConstants.NT_SLING_ORDEREDFOLDER, false);
                if (parentNode != null) {
                    String itemTitle = gcItem.getName();
                    Node itemNode = JcrUtil.createPath(parentNode, gcItem.getId(), false,
                            DamConstants.NT_SLING_ORDEREDFOLDER, DamConstants.NT_SLING_ORDEREDFOLDER, session, false);
                    Node itemNodeContent = JcrUtil.createPath(itemNode, JcrConstants.JCR_CONTENT, false,
                            JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, session, false);
                    String itemNodeContentTitle =
                            JcrUtils.getStringProperty(itemNodeContent, JcrConstants.JCR_TITLE, "");
                    if (!itemTitle.equals(itemNodeContentTitle)) {
                        itemNodeContent.setProperty(JcrConstants.JCR_TITLE, itemTitle);
                    }
                    String gcFilename = gcFile.getFilename();
                    int pos = gcFilename.lastIndexOf(".");
                    String gcFilenameTitle = pos > 0 ? gcFilename.substring(0, pos) : gcFilename;
                    Node assetNode = JcrUtil.createPath(itemNode, gcFile.getId(), false,
                            DamConstants.NT_SLING_ORDEREDFOLDER, DamConstants.NT_SLING_ORDEREDFOLDER, session, false);
                    Node assetNodeContent = JcrUtil.createPath(assetNode, JcrConstants.JCR_CONTENT, false,
                            JcrConstants.NT_UNSTRUCTURED, JcrConstants.NT_UNSTRUCTURED, session, false);
                    String assetNodeContentTitle =
                            JcrUtils.getStringProperty(assetNodeContent, JcrConstants.JCR_TITLE, "");
                    if (!gcFilenameTitle.equals(assetNodeContentTitle)) {
                        assetNodeContent.setProperty(JcrConstants.JCR_TITLE, gcFilenameTitle);
                    }
                    session.save();
                    path = assetNode.getPath() + "/" + gcFilename;
                } else {
                    LOGGER.error("Can not get or create importDAMPath: {}", importDAMPath);
                }
            } else {
                LOGGER.error("Can not get: {}", DamConstants.MOUNTPOINT_ASSETS);
            }
        } catch (LoginException | RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
            sw.stop();
            LOGGER.debug("Execution time: createAssetFolderStructure - {} ms", sw.getTime());
        }
        return path;
    }

    private ImportResultItem createUpdatePage(final GCContext gcContext, final ResourceResolver resourceResolver,
                                              final GCItem gcItem, final ImportItem importItem,
                                              final String importDAMPath, final Map<String, String> mapping,
                                              final Map<String, String> metaMapping) {
        StopWatch sw = new StopWatch();
        sw.start();
        long prevTime = 0;
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        try {
            Page targetPage = pageManager.getPage(importItem.getImportPath());

            pageManager.createRevision(targetPage);
            Map<String, Asset> gcAssets = createGCAssets(gcContext, gcItem, importDAMPath);
            prevTime = sw.getTime();
            ModifiableValueMap modifiableValueMap = targetPage.getContentResource().adaptTo(ModifiableValueMap.class);
            addGCProperties(modifiableValueMap, gcItem.getProjectId(), gcItem.getId(), importItem.getMappingPath());
            modifiableValueMap.put(NameConstants.PN_PAGE_LAST_MOD, Calendar.getInstance());
            //update properties based on template
            Collection<String> updatedProperties = new HashSet<>();
            for (Map.Entry<String, String> mapEntry : metaMapping.entrySet()) {
                if (!mapEntry.getValue().isEmpty()) {
                    updateMetaProperty(gcItem, targetPage, mapEntry.getKey(), mapEntry.getValue(), updatedProperties);
                }
            }
            for (Map.Entry<String, String> mapEntry : mapping.entrySet()) {
                if (!mapEntry.getValue().isEmpty()) {
                    updateProperty(gcItem, targetPage, mapEntry.getKey(), mapEntry.getValue(), gcAssets,
                            updatedProperties);
                }
            }
            LOGGER.debug("Execution time: createUpdatePage:"
                    + " Updating properties of AEM page - {} ms", sw.getTime() - prevTime);
            prevTime = sw.getTime();
            resourceResolver.commit();
            LOGGER.debug("Execution time: createUpdatePage:"
                    + " resourceResolver.commit of updated properties of AEM page - {} ms", sw.getTime() - prevTime);
            Boolean updateItemStatus = false;
            if (importItem.getNewStatusData().getId() != null) {
                updateItemStatus =
                        gcContentApi.updateItemStatus(gcContext, gcItem.getId(), importItem.getNewStatusData().getId());
            }
            final GCData statusData = updateItemStatus ? importItem.getNewStatusData() : gcItem.getStatus().getData();
            return new ImportResultItem(statusData.getName(), gcItem.getName(), ImportResultItem.IMPORTED,
                    importItem.getTemplate(), "https://" + importItem.getSlug()
                    + ".gathercontent.com/item/" + gcItem.getId(), targetPage.getPath(), statusData.getColor());
        } catch (PersistenceException | WCMException | RepositoryException | LoginException | GCException e) {
            LOGGER.error(e.getMessage(), e);
            return new ImportResultItem(gcItem.getStatus().getData().getName(), gcItem.getName(),
                    ImportResultItem.NOT_IMPORTED, importItem.getTemplate(), null, null,
                    gcItem.getStatus().getData().getColor());
        } finally {
            sw.stop();
            LOGGER.debug("Execution time: createUpdatePage method - {} ms",
                    importItem.getImportPath(), sw.getTime());
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

    //We need to update property based on update strategy
    private void updateProperty(final GCItem gcItem, final Page page, final String key, final String propertyPath,
                                final Map<String, Asset> gcAssets, final Collection<String> updatedProperties) {
        GCElement gcElement = findByKey(gcItem, key);
        if (gcElement != null) {
            String elementValue = gcElement.getValue();
            GCElementType gcElementType = gcElement.getType();
            Node node = page.adaptTo(Node.class);
            try {
                if (gcElementType != null) {
                    switch (gcElementType) {
                        case FILES:
                            if (!gcAssets.isEmpty()) {
                                Asset asset = gcAssets.get(key);
                                if (asset != null) {
                                    setProperty(node, propertyPath, asset.getPath(), false, updatedProperties);
                                }
                            }
                            break;
                        case CHOICE_CHECKBOX:
                        case CHOICE_RADIO:
                            setPropertyWithOptions(node, propertyPath, gcElement);
                            break;
                        case SECTION:
                            setProperty(node, propertyPath, gcElement.getSubtitle(), false, updatedProperties);
                            break;
                        case TEXT:
                        default:
                            setProperty(node, propertyPath, elementValue, true, updatedProperties);
                            break;
                    }
                } else {
                    LOGGER.error("GC Element type has unknown type");
                }
            } catch (RepositoryException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void setPropertyWithOptions(final Node node, final String propertyPath, final GCElement gcElement)
            throws RepositoryException {
        if (node.hasProperty(propertyPath)) {
            String relativePath = propertyPath.substring(0, propertyPath.lastIndexOf("/"));
            String propertyName = propertyPath.substring(propertyPath.lastIndexOf("/") + 1, propertyPath.length());
            Node destinationNode = node.getNode(relativePath);
            List<GCOption> options = gcElement.getOptions();
            List<String> optionLabels = new ArrayList<>();
            List<String> selectedLabels = new ArrayList<>();
            for (GCOption option : options) {
                //if we have not empty 'value' field use it. It is for (choice_radio = other) case
                String value = option.getValue() != null && !option.getValue().isEmpty() ? option.getValue()
                        : option.getLabel();
                optionLabels.add(value);
                if (option.getSelected()) {
                    selectedLabels.add(value);
                }
            }
            for (final ListIterator<String> i = optionLabels.listIterator(); i.hasNext();) {
                final String optionLabel = i.next();
                i.set(optionLabel.replace("=", "\\="));
            }
            setMultipleStringProperty(destinationNode, propertyName, optionLabels);
            setMultipleStringProperty(destinationNode, Constants.DEFAULT_SELECTION_PN, selectedLabels);
        } else {
            LOGGER.warn("Property '{}' is absent in AEM template. "
                    + "Possibly AEM template was modified after mapping. Please review", propertyPath);
        }
    }

    private void setProperty(final Node node, final String propertyPath, final String value,
                             final boolean needConcatenation, final Collection<String> updatedProperties)
            throws RepositoryException {
        if (node.hasProperty(propertyPath)) {
            String relativePath = propertyPath.substring(0, propertyPath.lastIndexOf("/"));
            String property = propertyPath.substring(propertyPath.lastIndexOf("/") + 1, propertyPath.length());
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

    /**
     * @inheritDoc
     */
    @Override
    public Map<String, String> getFieldMapping(final GCElementType gcElementType, final String templatePath)
            throws LoginException, RepositoryException {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getPageCreatorResourceResolver();
            Map<String, String> fieldMapping = new TreeMap<>();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = pageManager.getPage(templatePath);
            if (page != null && page.hasContent()) {
                Node aemTemplateNode = page.getContentResource().adaptTo(Node.class);
                if (aemTemplateNode != null) {
                    List<Property> allPropertiesList = new ArrayList<>();
                    getAllChildrenProperties(allPropertiesList, aemTemplateNode);

                    fieldMapping.put("", "Don't map");
                    List<Property> filteredProperties = new ArrayList<>(allPropertiesList);
                    switch (gcElementType) {
                        case FILES:
                            for (FieldFilter filter : getFilterListForText()) {
                                filteredProperties = filter.filter(filteredProperties);
                            }
                            break;
                        case CHOICE_CHECKBOX:
                        case CHOICE_RADIO:
                            for (FieldFilter filter : getFilterListForSelections()) {
                                filteredProperties = filter.filter(filteredProperties);
                            }
                            break;
                        case TEXT:
                        case SECTION:
                        default:
                            for (FieldFilter filter : getFilterListForText()) {
                                filteredProperties = filter.filter(filteredProperties);
                            }
                    }
                    for (Property filteredProperty : filteredProperties) {
                        String key = filteredProperty.getPath().replaceFirst(templatePath + "/", "");
                        String propertyName = filteredProperty.getName();
                        String fieldValue = "";
                        if (filteredProperty.isMultiple()) {
                            String[] values = PropertiesUtil.toStringArray(filteredProperty.getValues(), new String[0]);
                            if (values.length > 0) {
                                StringBuilder fieldStringBuilder = new StringBuilder();
                                for (String value : values) {
                                    fieldStringBuilder.append(value).append(" ");
                                }
                                fieldValue = fieldStringBuilder.toString();
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
            } else {
                LOGGER.error(String.format("No AEM template page was found at %s. Can not do mapping", templatePath));
            }
            return fieldMapping;
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

    private ResourceResolver getPageCreatorResourceResolver() throws LoginException {
        return ResourceResolverUtil.getResourceResolver(resourceResolverFactory, PAGE_CREATOR);
    }
}
