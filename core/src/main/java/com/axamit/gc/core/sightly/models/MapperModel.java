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
import com.axamit.gc.core.pojo.FieldMappingProperties;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.services.plugins.GCPluginManager;
import com.axamit.gc.core.services.AEMPageModifier;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Sling model class which represents mapping configuration.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = Resource.class)
public class MapperModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperModel.class);

    private final Resource resource;
    @Inject
    private AEMPageModifier aemPageModifier;
    @Inject
    private GCContentApi gcContentApi;
    @Inject
    private GCContentNewApi gcContentNewApi;
    @Inject
    private GCPluginManager gcPluginManager;
    @Inject
    private GCConfiguration gcConfiguration;
    @Inject
    @Optional
    private String projectName;
    @Inject
    @Optional
    private String mappingName;
    @Inject
    @Optional
    private Integer projectId;
    @Inject
    @Optional
    private Integer templateId;
    @Inject
    @Optional
    private String mappingTypeStr;
    @Inject
    @Optional
    private String importPath;
    @Inject
    @Optional
    private String importDAMPath;
    @Inject
    @Optional
    private String mapperStr;
    @Inject
    @Optional
    private String metaMapperStr;
    @Inject
    @Optional
    private String templateName;
    @Inject
    @Optional
    private String templatePath;
    @Inject
    @Optional
    private String abstractTemplateLimitPath;
    @Inject
    @Optional
    private String lastMapped;
    @Inject
    @Optional
    private String pluginConfigPath;
    @Inject
    @Optional
    @Named(Constants.EXPORT_OR_IMPORT_MAPPING_TYPE_PN)
    private String type;
    private MappingType mappingType;

    private Map<String, Map<String, String>> fieldsMappings;

    private Integer accountId;

    private GCTemplate gcTemplate;

    private GCItem customItem;

    private List<GCTemplateField> gcTemplateFields;

    private Map<Integer, String> projects;

    private Map<Integer, String> templates;

    private Map<String, FieldMappingProperties> mapper;

    private PluginsConfigurationListModel pluginsConfigurationListModel;

    private Map<String, String> metaMapper;

    private GCContext gcContext;

    /**
     * Constructor with resource initializing.
     *
     * @param resource <code>{@link Resource}</code> object.
     */
    public MapperModel(final Resource resource) {
        this.resource = resource;
    }

    private PluginsConfigurationListModel detect(Resource configResource) {
        final ResourceResolver resourceResolver = configResource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page containingPage = pageManager.getContainingPage(configResource);
        Resource configListResource = containingPage.getContentResource().getChild(Constants.PLUGINS_CONFIG_LIST_NN);
        if (configListResource != null) {
            PluginsConfigurationListModel model = configListResource.adaptTo(PluginsConfigurationListModel.class);
            if (model != null && !model.isEmpty()) {
                return model;
            }
        }
        Resource defaultPluginsConfig =
            gcPluginManager.getOrCreateDefaultPluginsConfig(resourceResolver, containingPage.getContentResource());
        if (defaultPluginsConfig == null) {
            //! Log
            return null;
        }
        return defaultPluginsConfig.getParent().adaptTo(PluginsConfigurationListModel.class);
    }

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        gcContext = gcConfiguration.getGCContext(resource);
        accountId = gcConfiguration.getAccountId(resource);
        mappingType = Objects.firstNonNull(MappingType.of(mappingTypeStr), MappingType.TEMPLATE);
        pluginsConfigurationListModel = detect(resource);
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(final Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final Integer templateId) {
        this.templateId = templateId;
    }

    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(final String importPath) {
        this.importPath = importPath;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(final String templatePath) {
        this.templatePath = templatePath;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(final Integer accountId) {
        this.accountId = accountId;
    }

    /**
     * @return GatherContent template object of <code>{@link GCTemplate}</code> type.
     */
    private GCTemplate getGcTemplate() {
        if (gcTemplate == null
                && StringUtils.isNotEmpty(templatePath)
                && projectId != null && projectId != 0
                && accountId != null && accountId != 0
                && templateId != null && templateId != 0
                && MappingType.TEMPLATE == mappingType) {
            try {
                gcTemplate = gcContentNewApi.template(gcContext, templateId);
                templateName = gcTemplate.getData().getName();
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return gcTemplate;
    }

    public void setGcTemplate(final GCTemplate gcTemplate) {
        this.gcTemplate = gcTemplate;
    }

    private GCItem getCustomItem() {
        if (customItem == null
                && StringUtils.isNotEmpty(templatePath)
                && projectId != null && projectId != 0
                && accountId != null && accountId != 0
                && templateId != null && templateId != 0
                && (MappingType.ENTRY_PARENT == mappingType || MappingType.CUSTOM_ITEM == mappingType)) {
            try {
                customItem = gcContentNewApi.itemById(gcContext, templateId);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return customItem;
    }

    public void setCustomItem(GCItem customItem) {
        this.customItem = customItem;
    }

    /**
     * @return List of GatherContent <code>{@link GCTemplateGroup}</code> objects of mapped Item/Template.
     */
    public List<GCTemplateField> getGcTemplateFields() {
        if (gcTemplateFields == null
                && StringUtils.isNotEmpty(templatePath)
                && accountId != null && accountId != 0
                && projectId != null && projectId != 0
                && templateId != null && templateId != 0) {
            switch (mappingType) {
                //TODO
//                case ENTRY_PARENT:
//                case CUSTOM_ITEM:
//                    customItem = getCustomItem();
//                    if (customItem != null) {
//                        gcGroups = customItem.getConfig();
//                    }
//                    return gcTemplateFields;
                case TEMPLATE:
                default:
                    gcTemplate = getGcTemplate();
                    if (gcTemplate != null) {
                        gcTemplateFields = GCUtil.getFieldsByTemplate(gcTemplate);
                    }
                    return gcTemplateFields;
            }
        }
        return gcTemplateFields;
    }

    public void setGcTemplateFields(List<GCTemplateField> gcTemplateFields) {
        this.gcTemplateFields = gcTemplateFields;
    }

    public List<GCTemplateGroup> getGcTemplateGroups() {
        return gcTemplate.getRelated().getStructure().getGroups();
    }

    /**
     * @return Map with GatherContent field name ID as a key and AEM field name as a value.
     */
    public Map<String, FieldMappingProperties> getMapper() {
        if (mapper == null && mapperStr != null) {
            try {
                mapper = JSONUtil.fromJsonToMappingMap(mapperStr);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return mapper;
    }

    public void setMapper(final Map<String, FieldMappingProperties> mapper) {
        this.mapper = mapper;
    }

    /**
     * @return Map with meta field name as a key and AEM field name as a value.
     */
    public Map<String, String> getMetaMapper() {
        if (metaMapperStr == null) {
            return Collections.emptyMap();
        }
        if (metaMapper == null) {
            try {
                metaMapper = JSONUtil.fromJsonToMapObject(metaMapperStr, String.class, String.class);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return metaMapper;
    }

    public void setMetaMapper(final Map<String, String> metaMapper) {
        this.metaMapper = metaMapper;
    }

    /**
     * @return Collection of GatherContent field and accordingly AEM fields which could be mapped to this GatherContent
     * field.
     */
    public Map<String, Map<String, String>> getFieldsMappings() {
        if (fieldsMappings == null) {
            gcTemplateFields = getGcTemplateFields();
            if (gcTemplateFields != null && StringUtils.isNotEmpty(templatePath)) {
                try {
                    fieldsMappings = aemPageModifier
                        .getFieldsMappings(gcTemplateFields, false, true, templatePath, getPluginConfigPath(),
                            StringUtils.EMPTY);
                } catch (LoginException | RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return fieldsMappings;
    }

    /**
     * @return Collection of GatherContent field and accordingly abstract AEM template page fields which
     * could be mapped to this GatherContent field.
     */
    public Map<String, Map<String, String>> getAbsFieldsMappings() {
        if (fieldsMappings == null) {
            gcTemplateFields = getGcTemplateFields();
            if (gcTemplateFields != null && StringUtils.isNotEmpty(templatePath)) {
                try {
                    fieldsMappings = aemPageModifier
                        .getFieldsMappings(gcTemplateFields, true, false, templatePath, getPluginConfigPath(),
                            abstractTemplateLimitPath);
                } catch (LoginException | RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return fieldsMappings;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getMappingName() {
        return mappingName;
    }

    public String getMappingTypeStr() {
        return mappingTypeStr;
    }

    public void setMappingTypeStr(String mappingTypeStr) {
        this.mappingTypeStr = mappingTypeStr;
    }

    /**
     * @return Calling <code>{@link Page#getTitle()}</code> if AEM 'template page'.
     */
    public String getAemTemplatePageTitle() {
        String aemTemplatePageTitle = null;
        if (StringUtils.isNotEmpty(templatePath)) {
            PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
            Page page = pageManager.getPage(templatePath);
            if (page != null) {
                aemTemplatePageTitle = page.getTitle();
            }
        }
        return aemTemplatePageTitle;
    }

    /**
     * @return Date of last modification of template in GatherContent.
     */
    public String getLastUpdated() {
        switch (mappingType) {
            //TODO
//            case ENTRY_PARENT:
//            case CUSTOM_ITEM:
//                customItem = getCustomItem();
//                if (customItem != null && customItem.getUpdatedAt() != null) {
//                    return customItem.getUpdatedAt();
//                }
            case TEMPLATE:
            default:
                gcTemplate = getGcTemplate();
                if (gcTemplate != null && !StringUtils.isEmpty(gcTemplate.getData().getUpdatedAt())) {
                    return gcTemplate.getData().getUpdatedAt();
                }
        }
        return Constants.UNAVAILABLE_TEMPLATE;
    }

    /**
     * @return Date of last modification of mapping.
     */
    public String getLastMapped() {
        if (lastMapped != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.ITEM_DATE_FORMAT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(lastMapped));
            return dateFormat.format(calendar.getTime());
        } else {
            return StringUtils.EMPTY;
        }
    }

    public void setLastMapped(final String lastMapped) {
        this.lastMapped = lastMapped;
    }

    /**
     * @return Map with GatherContent project ID as a key and GatherContent project name as a value.
     */
    public Map<Integer, String> getProjects() {
        if (projects == null && accountId != null) {
            try {
                List<GCProject> gcProjects = gcContentApi.projects(gcContext, accountId);
                projects = new HashMap<>();
                for (GCProject gcProject : gcProjects) {
                    projects.put(gcProject.getId(), gcProject.getName());
                }
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return projects;
    }

    /**
     * @return Map with GatherContent template ID as a key and GatherContent template name as a value.
     */
    public Map<Integer, String> getTemplates() {
        if (templates == null) {
            try {
                if (projectId != null && projectId != 0) {
                    templates = new HashMap<>();
                    switch (mappingType) {
                        //TODO
//                        case ENTRY_PARENT:
//                            List<GCItemNew> gcEntryParentItems = gcContentNewApi.itemsByProjectId(gcContext, projectId);
//                            for (GCItemNew gcItem : gcEntryParentItems) {
//                                if (GCItemType.ENTRY_PARENT == gcItem.getItemType()) {
//                                    templates.put(gcItem.getId(), gcItem.getName());
//                                }
//                            }
//                            break;
//                        case CUSTOM_ITEM:
//                            List<GCItemNew> gcCustomItems = gcContentNewApi.itemsByProjectId(gcContext, projectId);
//                            for (GCItemNew gcItem : gcCustomItems) {
//                                if (GCItemType.ITEM == gcItem.getItemType() && gcItem.getTemplateId() == null) {
//                                    templates.put(gcItem.getId(), gcItem.getName());
//                                }
//                            }
//                            break;
                        case TEMPLATE:
                        default:
                            List<GCTemplateData> gcTemplates = gcContentNewApi.templates(gcContext, projectId);
                            for (GCTemplateData gcTemplate : gcTemplates) {
                                templates.put(gcTemplate.getId(), gcTemplate.getName());
                            }
                            break;
                    }
                }
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return templates;
    }

    public String getImportDAMPath() {
        return importDAMPath;
    }

    public void setImportDAMPath(final String importDAMPath) {
        this.importDAMPath = importDAMPath;
    }

    /**
     * @return Link to mapping edit page.
     */
    public String getCreateEditlink() {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(resource);
        String mappingSelector = Constants.MAPPING_IMPORT_SELECTOR;
        if (Constants.MAPPING_TYPE_EXPORT.equals(type)) {
            mappingSelector = Constants.MAPPING_EXPORT_SELECTOR;
        }
        return page.getPath() + "." + mappingSelector + ".mapping-" + resource.getName() + ".html";
    }

    public Resource getResource() {
        return resource;
    }

    /**
     * @return Model consists list of Plugins configurations for current cloud service configuration.
     */
    public PluginsConfigurationListModel getPluginsConfigurationListModel() {
        return pluginsConfigurationListModel;
    }

    /**
     * @return JCR path to selected Plugins configuration.
     */
    public String getPluginConfigPath() {
        if (pluginConfigPath == null) {
            PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
            Page containingPage = pageManager.getContainingPage(resource);
            Resource defaultPluginsConfig =
                gcPluginManager.getOrCreateDefaultPluginsConfig(resource.getResourceResolver(),
                    containingPage.getContentResource());
            if (defaultPluginsConfig != null) {
                pluginConfigPath = defaultPluginsConfig.getPath();
            }
        }
        return pluginConfigPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
