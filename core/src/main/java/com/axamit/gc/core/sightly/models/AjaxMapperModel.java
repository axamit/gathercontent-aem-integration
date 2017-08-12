/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.MappingType;
import com.axamit.gc.core.services.GCPluginManager;
import com.axamit.gc.core.services.PageCreator;
import com.axamit.gc.core.util.Constants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.commons.lang3.StringUtils;
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
    private PageCreator pageCreator;

    @Inject
    private GCContentApi gcContentApi;

    @Inject
    private GCConfiguration gcConfiguration;

    @Inject
    private GCPluginManager gcPluginManager;

    @Inject
    private SlingHttpServletRequest request;

    private Map<String, Map<String, String>> fieldsMappings;

    private String templateName;

    private String templatePath;

    private List<GCConfig> gcConfigs;

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
        String projectId = request.getParameter(Constants.GC_PROJECT_ID_PN);
        String templateId = request.getParameter(Constants.GC_TEMPLATE_ID_PN);
        String mappingTypeParam = request.getParameter(Constants.MAPPING_TYPE_PARAM_NAME);
        pluginConfigPath = request.getParameter(Constants.GC_PLUGIN_CONFIG_PATH_PARAM_NAME);
        if (StringUtils.isEmpty(pluginConfigPath)) {
            pluginConfigPath = getDefaultPluginConfigPath();
        }
        MappingType mappingType = MappingType.of(mappingTypeParam);
        mappingType = mappingType != null ? mappingType : MappingType.TEMPLATE;
        if (StringUtils.isNotEmpty(projectId) && StringUtils.isNotEmpty(templateId)) {
            switch (mappingType) {
                case CUSTOM_ITEM:
                case ENTRY_PARENT:
                    try {
                        GCItem gcItem = gcContentApi.itemById(gcContext, templateId);
                        gcConfigs = gcItem.getConfig();
                        templateName = gcItem.getName();
                    } catch (GCException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    break;
                case TEMPLATE:
                default:
                    try {
                        GCTemplate gctemplate = gcContentApi.template(gcContext, projectId, templateId);
                        gcConfigs = gctemplate.getConfig();
                        templateName = gctemplate.getName();
                        return;
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
            gcConfigs = getGcConfigs();
            if (gcConfigs != null && StringUtils.isNotEmpty(templatePath)) {
                try {
                    fieldsMappings = pageCreator.getFieldsMappings(gcConfigs, false, true, templatePath, pluginConfigPath, StringUtils.EMPTY);
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
            gcConfigs = getGcConfigs();
            if (gcConfigs != null && StringUtils.isNotEmpty(templatePath)) {
                try {
                    fieldsMappings = pageCreator.getFieldsMappings(gcConfigs, true, false, templatePath, pluginConfigPath, abstractTemplateLimitPath);
                } catch (LoginException | RepositoryException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return fieldsMappings;
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

    public List<GCConfig> getGcConfigs() {
        return gcConfigs;
    }

    public void setGcConfigs(List<GCConfig> gcConfigs) {
        this.gcConfigs = gcConfigs;
    }

}
