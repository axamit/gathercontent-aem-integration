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
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.services.AbstractPageModifier;
import com.axamit.gc.core.services.GCPageModifier;
import com.axamit.gc.core.services.plugins.GCPluginManager;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.axamit.gc.core.sightly.models.MapperModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.ResourceResolverUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The <tt>GCItemCreator</tt> interface provides methods to create items in GatherContent.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPageModifier.class)
@Component(description = "GC Page Modifier Service", name = "GC Page Modifier", immediate = true, metatype = true)
public final class GCPageModifierImpl extends AbstractPageModifier implements GCPageModifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCPageModifierImpl.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private GCContentApi gContentApi;

    @Reference
    private GCContentNewApi gcContentNewApi;

    @Reference
    private GCPluginManager gcPluginManager;

    private static GCItem createGCItem(final Integer projectId, final MapperModel mapperModel, final String folderUuid,
                                       final String name) {
        GCItem gcItem = new GCItem();
        if (mapperModel != null && MappingType.TEMPLATE == mapperModel.getMappingType()) {
            gcItem.setTemplateId(mapperModel.getTemplateId());
        }
//        if (mapperModel != null) {
//            gcItem.setConfig(mapperModel.getGcGroups());
//        }
        gcItem.setProjectId(projectId);
        gcItem.setFolderUuid(folderUuid);
        gcItem.setName(name);

        return gcItem;
    }

    private static String findOutFolderOrPage(PageManager pageManager, ImportItem importItem) {
        return pageManager.getPage(importItem.getImportPath()) == null ? "folder" : "page";
    }

    private static MapperModel getMapperModel(ResourceResolver resourceResolver, ImportItem importItem) {
        Resource mappingResource = resourceResolver.resolve(importItem.getMappingPath());
        if (mappingResource == null || ResourceUtil.isNonExistingResource(mappingResource)) {
            LOGGER.error("Mapping \"{}\" not found", importItem.getMappingPath());
            return null;
        }
        MapperModel mapperModel = mappingResource.adaptTo(MapperModel.class);
        if (mapperModel == null) {
            LOGGER.error("Can not adapt mapping \"{}\" to model {}", importItem.getMappingPath(),
                    MapperModel.class.getName());
            return null;
        }
        return mapperModel;
    }

    private static void updateGCSpecialPropertiesInAEMPage(ResourceResolver resourceResolver, PageManager pageManager,
                                                           Integer projectId, Integer itemId, ImportItem importItem) {
        ModifiableValueMap modifiableValueMap = pageManager.getPage(importItem.getImportPath()) != null
                ? pageManager.getPage(importItem.getImportPath()).getContentResource().adaptTo(ModifiableValueMap.class)
                : resourceResolver.getResource(importItem.getImportPath()).adaptTo(ModifiableValueMap.class);
        try {
            GCUtil.addGCExportProperties(resourceResolver, modifiableValueMap, projectId,
                    itemId, importItem.getMappingPath() == null ? StringUtils.EMPTY : importItem.getMappingPath());
        } catch (PersistenceException | GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    private void updateGCProperty(final GCItem gcItem, final Page page, final String key,
                                  final FieldMappingProperties fieldMappingProperties,
                                  final ResourceResolver resourceResolver, final String configurationPath, final GCContext gcContext) {
        GCContent gcContent = findContentByKey(gcItem, key);
        if (gcContent != null) {
            if (!fieldMappingProperties.getPath().isEmpty()) {
                for (String relativePropertyPath : fieldMappingProperties.getPath()) {
                    if (!ResourceUtil.isNonExistingResource(resourceResolver.resolve(
                            GCStringUtil.appendNewLevelToPath(page.getPath(), relativePropertyPath)))) {
                        String fullPath = GCStringUtil.appendNewLevelToPath(page.getPath(), relativePropertyPath);
                        String propertyPath = GCStringUtil.getRelativeNodePathFromPropertyPath(fullPath);
                        String propertyValue = GCStringUtil.getPropertyNameFromPropertyPath(fullPath);

                        try {
                            GCPlugin gcPlugin = gcPluginManager.getPlugin(resourceResolver, configurationPath,
                                    gcContent.getType().getValue(), StringUtils.EMPTY, fieldMappingProperties.getPlugin());
                            if (isNewEditorMultifieldElement(gcContext, gcContent)) {
                                //TODO
//                                gcContent.setType(GCElementType.MULTIVALUE_NEW_EDITOR);
                            }
                            if (gcPlugin != null) {
                                gcPlugin.transformFromAEMtoGC(resourceResolver, page, gcContent, propertyPath, propertyValue);
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        break;
                    }
                }
            } else if (gcItem.getTemplateId() == null) {
                if (GCElementType.TEXT.equals(gcContent.getType())) {
                    gcContent.setText(StringUtils.EMPTY);
                } /*else if (GCElementType.SECTION.equals(gcContent.getType())) {*/
                    //TODO
//                    gcContent.setSubtitle(StringUtils.EMPTY);
//                }
            }
        }
    }

    private boolean isNewEditorMultifieldElement(GCContext gcContext, GCContent gcContent) {
        GCElementType elementType = gcContent.getType();
        return (GCElementType.CHOICE_CHECKBOX.equals(elementType) || GCElementType.CHOICE_RADIO.equals(elementType));
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<ImportResultItem> createPage(final List<ImportItem> importItemsToMerge, final GCContext gcContext,
                                             final List<ImportItem> childrenItems, final Integer projectId) {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = getPageCreatorResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            GCItem gcItem = createMergedGCItem(importItemsToMerge, projectId, resourceResolver, pageManager, gcContext);
            if (gcItem != null) { //! Else? I would log and early break/return
                Integer createdItemId = gcContentNewApi.createItem(gcItem, gcContext);
                if (createdItemId != null) { //! Else? I would log and early break/return
                    GCStatus statusData = new GCStatus();
                    if (!importItemsToMerge.isEmpty() && importItemsToMerge.get(0).getNewStatusData().getId() != null
                            && gContentApi.updateItemStatus(gcContext, createdItemId,
                            importItemsToMerge.get(0).getNewStatusData().getId())) {
                        statusData = importItemsToMerge.get(0).getNewStatusData();
                    } else {
                        List<GCStatus> gcStatusList = gContentApi.statusesByProjectId(gcContext, projectId);
                        for (GCStatus gcStatus : gcStatusList) {
                            if (gcStatus.getIsDefault()) {
                                statusData = gcStatus;
                                break;
                            }
                        }
                    }
                    for (ImportItem childItem : childrenItems) {
                        childItem.setGcTargetItemId(createdItemId);
                    }
                    ImmutableList.Builder<ImportResultItem> importResultItemList = ImmutableList.builder();
                    for (ImportItem importItem : importItemsToMerge) {
                        updateGCSpecialPropertiesInAEMPage(resourceResolver, pageManager, gcItem.getProjectId(),
                                createdItemId, importItem);
                        final ImportResultItem importResultItem = new ImportResultItem(statusData.getName(),
                                gcItem.getName(),
                                importItem.getAemTitle(),
                                ImportResultItem.IMPORTED,
                                importItem.getTemplate(),
                                //! OSGI configurable mask like "https://{0}.gathercontent.com/item/" would be great.
                                "https://" + importItem.getSlug() + ".gathercontent.com/item/" + createdItemId,
                                importItem.getImportPath(),
                                statusData.getColor(),
                                gcItem.getPosition(),
                                createdItemId,
//                                gcItem.getParentId(),
                                "0",
                                importItem.getImportIndex(),
                                importItem.getMappingName());
                        importResultItemList.add(importResultItem.setType(findOutFolderOrPage(pageManager, importItem)));
                    }
                    return importResultItemList.build();
                }
            }
        } catch (LoginException e) {
            LOGGER.error("Failed to get ServiceResourceResolver {}", e.getMessage());
        } catch (GCException e) {
            LOGGER.error("Failed to get get data from GC {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to export AEM page {}", e.getMessage());
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }

        ImmutableList.Builder<ImportResultItem> importResultItemList = ImmutableList.builder();
        for (ImportItem importItem : importItemsToMerge) {
            LOGGER.error("Couldn't export AEM page {}", importItem.getImportPath());
            importResultItemList.add(new ImportResultItem().setImportStatus(ImportResultItem.NOT_IMPORTED)
                    .setGcTemplateName(importItem.getTemplate()).setAemLink(importItem.getImportPath())
                    .setAemTitle(importItem.getAemTitle()).setImportIndex(importItem.getImportIndex()));
        }
        return importResultItemList.build();
    }

    private GCItem createMergedGCItem(final Iterable<ImportItem> importItemsToMerge, final Integer projectId,
                                      final ResourceResolver resourceResolver, final PageManager pageManager, final GCContext gcContext) {
        GCItem gcItem = null;
        for (ImportItem importItem : importItemsToMerge) {
            if (importItem.getMappingPath() != null) {
                Page page = pageManager.getPage(importItem.getImportPath());
                MapperModel mapperModel = getMapperModel(resourceResolver, importItem);
                if (mapperModel != null) {
                    Map<String, FieldMappingProperties> mapping = mapperModel.getMapper();
                    importItem.setMappingName(mapperModel.getMappingName());
                    String pluginConfigPath = mapperModel.getPluginConfigPath();
                    //! First-time create in loop?
                    if (gcItem == null) {
                        //TODO
//                        gcItem =
//                                createGCItem(projectId, mapperModel, importItem.getFolderUuid(), importItem.getTitle());
                    }
                    if (mapping != null && page != null) {
                        for (Map.Entry<String, FieldMappingProperties> mapEntry : mapping.entrySet()) {
                            updateGCProperty(gcItem, page, mapEntry.getKey(), mapEntry.getValue(), resourceResolver,
                                    pluginConfigPath, gcContext);
                        }
                    } else {
                        LOGGER.error("No mapped properties in the mapping \"{}\"", importItem.getMappingPath());
                        //! Break/return?
                    }
                }
            } else {
                //! Overwrite in loop?

                //TODO
//                gcItem = createGCItem(projectId, null, importItem.getFolderUuid(), importItem.getTitle());
            }
        }
        return gcItem;
    }

    /**
     * @inheritDoc
     */
    @Override
    public ImportResultItem updatePage(final GCContext gcContext, final ImportItem importItem) {
        ResourceResolver resourceResolver = null;
        String mappingName = null;
        try {
            resourceResolver = getPageCreatorResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = pageManager.getPage(importItem.getImportPath());

            MapperModel mapperModel = getMapperModel(resourceResolver, importItem);
            if (mapperModel != null) { //! Else? I would log and early break/return
                String pluginConfigPath = mapperModel.getPluginConfigPath();
                Map<String, FieldMappingProperties> mapping = mapperModel.getMapper();
                mappingName = mapperModel.getMappingName();

                GCItem gcItem = gcContentNewApi.itemById(gcContext, importItem.getItemId());

                if (gcItem != null) {
                    if (mapping == null) {
                        LOGGER.error("No mapped properties in the mapping \"{}\"", importItem.getMappingPath());
                        //! Break/return?
                    } else {
                        for (Map.Entry<String, FieldMappingProperties> mapEntry : mapping.entrySet()) {
                            updateGCProperty(gcItem, page, mapEntry.getKey(), mapEntry.getValue(),
                                    resourceResolver, pluginConfigPath, gcContext);
                        }
                    }
//TODO
//                    Boolean isUpdatedSuccessfully = gcContentNewApi.updateItem(gcItem.getConfig(), gcItem.getId(), gcContext);
                    boolean isUpdatedSuccessfully = false;

                    if (isUpdatedSuccessfully) {
                        boolean updateItemStatus = false;
                        if (importItem.getNewStatusData().getId() != null) {
                            updateItemStatus = gContentApi
                                    .updateItemStatus(gcContext, gcItem.getId(), importItem.getNewStatusData().getId());
                        }
                        //TODO
//                        final GCData statusData =
//                                updateItemStatus ? importItem.getNewStatusData() : gcItem.getStatus().getData();
//                        updateGCSpecialPropertiesInAEMPage(resourceResolver, pageManager, gcItem.getProjectId(),
//                                gcItem.getId(), importItem);
//                        return new ImportResultItem(statusData.getName(),
//                                gcItem.getName(),
//                                page.getTitle(),
//                                ImportResultItem.IMPORTED,
//                                importItem.getTemplate(),
//                                //! OSGI configurable mask like "https://{0}.gathercontent.com/item/" would be great.
//                                "https://" + importItem.getSlug() + ".gathercontent.com/item/" + gcItem.getId(),
//                                importItem.getImportPath(),
//                                statusData.getColor(),
//                                gcItem.getPosition(),
//                                gcItem.getId(),
//                                gcItem.getParentId(),
//                                importItem.getImportIndex(),
//                                mappingName);
                        return null;
                    } else {
                        LOGGER.error("Couldn't update item {}", importItem.getItemId());
                        //TODO
                        return null;
//                        return new ImportResultItem(null,
//                                null,
//                                null,
//                                ImportResultItem.NOT_IMPORTED,
//                                importItem.getTemplate(),
//                                "https://" + importItem.getSlug() + ".gathercontent.com/item/" + gcItem.getId(),
//                                importItem.getImportPath(),
//                                null,
//                                gcItem.getPosition(),
//                                gcItem.getId(),
//                                gcItem.getParentId(),
//                                importItem.getImportIndex(),
//                                mappingName);
                    }
                }
            }
        } catch (LoginException e) {
            LOGGER.error("Failed to get ServiceResourceResolver {}", e.getMessage());
        } catch (GCException e) {
            LOGGER.error("Failed to get get data from GC {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Failed to export update AEM page {}", e.getMessage());
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
        LOGGER.error("Couldn't update item {}", importItem.getItemId());
        return new ImportResultItem(null, null, null, ImportResultItem.NOT_IMPORTED, importItem.getTemplate(),
                null, importItem.getImportPath(), null, null, null, null, importItem.getImportIndex(), mappingName);
    }

    private ResourceResolver getPageCreatorResourceResolver() throws LoginException {
        return ResourceResolverUtil.getResourceResolver(resourceResolverFactory,
                Constants.PAGE_CREATOR_SUBSERVICE_NAME);
    }
}
