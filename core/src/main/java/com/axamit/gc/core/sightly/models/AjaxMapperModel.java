/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.dto.GCTemplateField;
import com.axamit.gc.api.dto.GCTemplateGroup;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentNewApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.services.plugins.GCPluginManager;
import com.axamit.gc.core.services.AEMPageModifier;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 * Sling model class which represents mapping between AEM and GatherContent fields which are requested via AJAX after
 * changing GatherContent or AEM template.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class AjaxMapperModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(AjaxMapperModel.class);

    @Inject
    private AEMPageModifier aemPageModifier;

    @Inject
    private GCContentNewApi gcContentNewApi;

    @Inject
    private GCConfiguration gcConfiguration;

    @Inject
    private GCPluginManager gcPluginManager;

    @Inject
    private SlingHttpServletRequest request;

    private Map<String, Map<String, String>> fieldsMappings;

    private String templateName;

    private String templatePath;

    private GCTemplate gcTemplate;

    private String abstractTemplateLimitPath;

    private String pluginConfigPath;

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        GCContext gcContext = gcConfiguration.getGCContext(request.getResource());
        templatePath = request.getParameter(Constants.AEM_TEMPLATE_PATH_PN);
        abstractTemplateLimitPath = request.getParameter(Constants.AEM_ABSTRACT_TEMPLATE_LIMIT_PATH);
        int projectId = NumberUtils.toInt(request.getParameter(Constants.GC_PROJECT_ID_PN), 0);
        int templateId = NumberUtils.toInt(request.getParameter(Constants.GC_TEMPLATE_ID_PN), 0);
        String mappingTypeParam = request.getParameter(Constants.MAPPING_TYPE_PARAM_NAME);
        pluginConfigPath = request.getParameter(Constants.GC_PLUGIN_CONFIG_PATH_PARAM_NAME);
        if (StringUtils.isEmpty(pluginConfigPath)) {
            pluginConfigPath = getDefaultPluginConfigPath();
        }
        MappingType mappingType = MappingType.of(mappingTypeParam);
        mappingType = mappingType != null ? mappingType : MappingType.TEMPLATE;
        if (projectId != 0 && templateId != 0) {
            switch (mappingType) {
                //TODO
//                case CUSTOM_ITEM:
//                case ENTRY_PARENT:
//                    try {
//                        GCItem gcItem = gcContentNewApi.itemById(gcContext, templateId);
//                        gcTemplateFields = gcItem.getConfig();
//                        templateName = gcItem.getName();
//                    } catch (GCException e) {
//                        LOGGER.error(e.getMessage(), e);
//                    }
//                    break;
                case TEMPLATE:
                default:
                    try {
                        gcTemplate = gcContentNewApi.template(gcContext, templateId);
                        templateName = gcTemplate.getData().getName();
                    } catch (GCException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    break;
            }
        }
    }

    /**
     * @return Collection of GatherContent field and accordingly AEM fields which could be mapped to this GatherContent
     * field.
     */
    public Map<String, Map<String, String>> getFieldsMappings() {
        if (fieldsMappings == null) {
            final List<GCTemplateField> gcTemplateFields = getGcTemplateFields();
            if (gcTemplateFields != null && StringUtils.isNotEmpty(templatePath)) {
                try {
                    fieldsMappings = aemPageModifier.getFieldsMappings(gcTemplateFields, false, true, templatePath, pluginConfigPath, StringUtils.EMPTY);
                } catch (LoginException | RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return ImmutableMap.copyOf(fieldsMappings);
    }

    /**
     * @return Collection of GatherContent field and accordingly abstract AEM template page fields which
     * could be mapped to this GatherContent field.
     */
    public Map<String, Map<String, String>> getAbsFieldsMappings() {
        if (fieldsMappings == null) {
            final List<GCTemplateField> gcTemplateFields = getGcTemplateFields();
            if (gcTemplateFields != null && StringUtils.isNotEmpty(templatePath)) {
                try {
                    fieldsMappings = aemPageModifier.getFieldsMappings(gcTemplateFields, true, false, templatePath, pluginConfigPath, abstractTemplateLimitPath);
                } catch (LoginException | RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return ImmutableMap.copyOf(fieldsMappings);
    }

    private String getDefaultPluginConfigPath() {
        if (pluginConfigPath == null) {
            Resource resource = request.getResource();
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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getAbstractTemplateLimitPath() {
        return abstractTemplateLimitPath;
    }

    public void setAbstractTemplateLimitPath(String abstractTemplateLimitPath) {
        this.abstractTemplateLimitPath = abstractTemplateLimitPath;
    }

    public List<GCTemplateField> getGcTemplateFields() {
        return GCUtil.getFieldsByTemplate(gcTemplate);
    }

    public List<GCTemplateGroup> getGcTemplateGroups() {
        return gcTemplate.getRelated().getStructure().getGroups();
    }
}
