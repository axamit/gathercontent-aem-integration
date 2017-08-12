/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.inject.Inject;

/**
 * Sling model class which represents export mapping configuration.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = Resource.class)
public class ExportMapperModel extends MapperModel {
    /**
     * Constructor with resource initializing.
     *
     * @param resource <code>{@link Resource}</code> object.
     */
    public ExportMapperModel(final Resource resource) {
        super(resource);
    }

    public String getAbstractTemplateLimitPath() {
        return abstractTemplateLimitPath;
    }

    public void setAbstractTemplateLimitPath(String abstractTemplateLimitPath) {
        this.abstractTemplateLimitPath = abstractTemplateLimitPath;
    }

    @Inject
    @Optional
    private String abstractTemplateLimitPath;
}
