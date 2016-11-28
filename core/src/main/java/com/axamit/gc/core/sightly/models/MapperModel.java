/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.services.PageCreator;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
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
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axamit.gc.core.util.Constants.NEVER;

/**
 * Sling model class which represents mapping configuration.
 */
@Model(adaptables = Resource.class)
public final class MapperModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MapperModel.class);
    private static final long MILLISECONDS_IN_SECOND = 1000;

    @Inject
    private PageCreator pageCreator;

    @Inject
    private GCContentApi gcContentApi;

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
    private String projectId;

    @Inject
    @Optional
    private String templateId;

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
    private String lastMapped;

    private Resource resource;

    private Map<String, Map<String, String>> fieldsMappings;

    private String accountId;

    private GCTemplate gctemplate;

    private Map<String, String> projects;

    private Map<String, String> templates;

    private Map<String, String> mapper;

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

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        gcContext = gcConfiguration.getGCContext(resource);
        accountId = gcConfiguration.getAccountId(resource);
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final String templateId) {
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return GatherContent template object of <code>{@link GCTemplate}</code> type.
     */
    public GCTemplate getGctemplate() {
        if (gctemplate == null && templatePath != null && projectId != null && accountId != null) {
            try {
                gctemplate = gcContentApi.template(gcContext, projectId, templateId);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return gctemplate;
    }

    public void setGctemplate(final GCTemplate gctemplate) {
        this.gctemplate = gctemplate;
    }

    /**
     * @return Map with GatherContent field name ID as a key and AEM field name as a value.
     */
    public Map<String, String> getMapper() {
        if (mapper == null && mapperStr != null) {
            try {
                mapper = JSONUtil.fromJsonToObject(mapperStr, Map.class);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return mapper;
    }

    public void setMapper(final Map<String, String> mapper) {
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
                metaMapper = JSONUtil.fromJsonToObject(metaMapperStr, Map.class);
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
        if (gctemplate != null && templatePath != null) {
            try {
                fieldsMappings = GCUtil.getFieldsMappings(gctemplate, pageCreator, templatePath);
            } catch (LoginException | RepositoryException e) {
                LOGGER.error(e.getMessage(), e);
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

    /**
     * @return Calling <code>{@link Page#getTitle()}</code> if AEM 'template page'.
     */
    public String getAemTemplatePageTitle() {
        String aemTemplatePageTitle = null;
        if (templatePath != null) {
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
        GCTemplate template = getGctemplate();
        if (template == null || StringUtils.isEmpty(template.getUpdatedAt())){
            return NEVER;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.OUTPUT_DATE_FORMAT);
        Date date = new Date(Long.parseLong(template.getUpdatedAt()) * MILLISECONDS_IN_SECOND);
        return dateFormat.format(date);
    }

    /**
     * @return Date of last modification of mapping.
     */
    public String getLastMapped() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.OUTPUT_DATE_FORMAT);
        if (lastMapped != null) {
            Date date = new Date(Long.parseLong(lastMapped));
            return dateFormat.format(date);
        }
        return null;
    }

    public void setLastMapped(final String lastMapped) {
        this.lastMapped = lastMapped;
    }

    /**
     * @return Map with GatherContent project ID as a key and GatherContent project name as a value.
     */
    public Map<String, String> getProjects() {
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
    public Map<String, String> getTemplates() {
        if (templates == null) {
            try {
                if (projectId != null) {
                    List<GCTemplate> gcTemplates = gcContentApi.templates(gcContext, projectId);
                    templates = new HashMap<>();
                    for (GCTemplate gcTemplate : gcTemplates) {
                        templates.put(gcTemplate.getId(), gcTemplate.getName());
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
        return page.getPath() + ".mapping.mapping-" + resource.getName() + ".html";
    }

    public Resource getResource() {
        return resource;
    }
}
