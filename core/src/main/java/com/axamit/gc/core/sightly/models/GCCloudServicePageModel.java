/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.core.sightly.helpers.Renderer;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Objects;

/**
 * Sling model class which represents table with items to process on import page.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Model(adaptables = SlingHttpServletRequest.class)
public final class GCCloudServicePageModel {

    @Inject
    private SlingHttpServletRequest request;

    private String resourceType;
    private String resourcePath;

    /**
     * Public Constructor.
     */
    public GCCloudServicePageModel() {
        // TODO document why this constructor is empty
    }

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        String[] selectors = request.getRequestPathInfo().getSelectors();
        switch (getRendererFromSelectorOrDefault()) {
            case MAPPING:
                this.resourcePath = Constants.MAPPING_LIST_NN;
                this.resourceType = Constants.MAPPING_LIST_RESOURCETYPE;

                setResourceFromSelector(selectors, Constants.MAPPING_SELECTOR,
                        Constants.MAPPING_RESOURCETYPE);
                break;
            case MAPPING_EXPORT:
                this.resourcePath = Constants.MAPPING_LIST_NN;
                this.resourceType = Constants.MAPPING_LIST_RESOURCETYPE;

                setResourceFromSelector(selectors, Constants.MAPPING_SELECTOR,
                        Constants.MAPPING_EXPORT_RESOURCETYPE);
                break;
            case JOBS:
                String suffix = request.getRequestPathInfo().getSuffix();
                this.resourcePath = Constants.IMPORT_LIST_NN;
                this.resourceType = Constants.IMPORT_LIST_RESOURCETYPE;

                if (suffix != null) {
                    this.resourcePath += suffix;
                    this.resourceType = Constants.IMPORT_ITEM_RESOURCETYPE;
                }
                break;
            case CONFIG:
                this.resourcePath = Constants.PLUGINS_CONFIG_LIST_NN;
                this.resourceType = Constants.PLUGINS_CONFIG_LIST_RESOURCETYPE;

                setResourceFromSelector(selectors, Constants.PLUGINS_CONFIG_SELECTOR,
                        Constants.PLUGIN_CONFIG_RESOURCETYPE);
                break;
            default:
                break;
        }
    }

    private void setResourceFromSelector(String[] selectors, String resourceSelector, String newResourceType) {
        if (selectors != null && StringUtils.isNotEmpty(resourceSelector) && StringUtils.isNotEmpty(newResourceType)) {
            for (String selector : selectors) {
                if (selector.startsWith(resourceSelector)) {
                    this.resourcePath = GCStringUtil.appendNewLevelToPath(resourcePath,
                            selector.substring(resourceSelector.length()));
                    this.resourceType = newResourceType;
                    break;
                }
            }
        }
    }

    /**
     * Return renderer depends on first selector or default.
     *
     * @return String with renderer name.
     */
    public String getRenderer() {
        return getRendererFromSelectorOrDefault().getType();
    }

    private Renderer getRendererFromSelectorOrDefault() {
        String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null && selectors.length > NumberUtils.INTEGER_ZERO) {
            return Arrays.stream(selectors).map(Renderer::of).filter(Objects::nonNull).findFirst().orElse(Renderer.DEFAULT);
        }
        return Renderer.DEFAULT;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

}
