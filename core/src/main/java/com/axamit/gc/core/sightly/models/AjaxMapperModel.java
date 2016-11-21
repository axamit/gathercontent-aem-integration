/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.api.services.GCConfiguration;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.services.PageCreator;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * Sling model class which represents mapping between AEM and GatherContent fields which are requested via AJAX after
 * changing GatherContent or AEM template.
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
    private SlingHttpServletRequest request;

    private Map<String, Map<String, String>> fieldsMappings;

    private GCTemplate gctemplate;

    private GCContext gcContext;

    private String templatePath;

    private String projectId;

    private String templateId;

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        gcContext = gcConfiguration.getGCContext(request.getResource());
        templatePath = request.getParameter(Constants.AEM_TEMPLATE_PATH_PN);
        projectId = request.getParameter(Constants.GC_PROJECT_ID_PN);
        templateId = request.getParameter(Constants.GC_TEMPLATE_ID_PN);
        if (projectId != null && !projectId.isEmpty() && templateId != null && !templateId.isEmpty()) {
            try {
                gctemplate = gcContentApi.template(gcContext, projectId, templateId);
            } catch (GCException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
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

    public GCTemplate getGctemplate() {
        return gctemplate;
    }

    public void setGctemplate(final GCTemplate gctemplate) {
        this.gctemplate = gctemplate;
    }

}
