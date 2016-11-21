/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The <code>ImportResultItem</code> represents result of an import of page.
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportResultItem {
    public static final String IMPORTED = "Imported";
    public static final String NOT_IMPORTED = "Not-imported";

    private String status;
    private String name;
    private String importStatus;
    private String gcTemplateName;
    private String gcLink;
    private String aemLink;
    private String color;

    /**
     * Public constructor.
     *
     * @param status         Live status in GatherContent.
     * @param name           Item name in GatherContent.
     * @param importStatus   Import status - 'Imported' or 'Not-imported'.
     * @param gcTemplateName Template name in GatherContent.
     * @param gcLink         Link to item in GatherContent.
     * @param aemLink        Link to page in AEM.
     * @param color          Color of Live status in GatherContent.
     */
    public ImportResultItem(final String status, final String name, final String importStatus,
                            final String gcTemplateName, final String gcLink, final String aemLink,
                            final String color) {
        this.status = status;
        this.name = name;
        this.importStatus = importStatus;
        this.gcTemplateName = gcTemplateName;
        this.gcLink = gcLink;
        this.aemLink = aemLink;
        this.color = color;
    }

    /**
     * @return Live status in GatherContent.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * @return Item name in GatherContent.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Import status - 'Imported' or 'Not-imported'.
     */
    public String getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(final String importStatus) {
        this.importStatus = importStatus;
    }

    /**
     * @return Template name in GatherContent.
     */
    public String getGcTemplateName() {
        return gcTemplateName;
    }

    public void setGcTemplateName(final String gcTemplateName) {
        this.gcTemplateName = gcTemplateName;
    }

    /**
     * @return Link to item in GatherContent.
     */
    public String getGcLink() {
        return gcLink;
    }

    public void setGcLink(final String gcLink) {
        this.gcLink = gcLink;
    }

    /**
     * @return Link to page in AEM.
     */
    public String getAemLink() {
        return aemLink;
    }

    public void setAemLink(final String aemLink) {
        this.aemLink = aemLink;
    }

    /**
     * @return Color of Live status in GatherContent.
     */
    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }
}
