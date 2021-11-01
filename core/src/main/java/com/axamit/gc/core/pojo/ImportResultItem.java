/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.axamit.gc.core.pojo.helpers.GCHierarchySortable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>ImportResultItem</code> represents result of an import of page.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportResultItem implements GCHierarchySortable {
    public static final String IMPORTED = "Imported";
    public static final String NOT_IMPORTED = "Not-imported";

    private String status;
    private String name;
    private String aemTitle;
    private String importStatus;
    private String gcTemplateName;
    private String gcLink;
    private String aemLink;
    private String color;
    private Integer position;
    private Integer id;
    private String folderUuid;
    private String type;
    private Integer importIndex;
    private String mappingName;

    /**
     * Default constructor.
     */
    public ImportResultItem() {
    }

    /**
     * Public constructor.
     *
     * @param status         Live status in GatherContent.
     * @param name           Item name in GatherContent.
     * @param aemTitle       Page title in AEM.
     * @param importStatus   Import status - 'Imported' or 'Not-imported'.
     * @param gcTemplateName Template name in GatherContent.
     * @param gcLink         Link to item in GatherContent.
     * @param aemLink        Link to page in AEM.
     * @param color          Color of Live status in GatherContent.
     * @param position       Position in GatherContent list of items.
     * @param id             GatherContent Item ID.
     * @param folderUuid       GatherContent
     * @param importIndex    Index number as it came to be processed.
     * @param mappingName    Mapping Name.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public ImportResultItem(final String status, final String name, final String aemTitle, final String importStatus,
                            final String gcTemplateName, final String gcLink, final String aemLink,
                            final String color, final Integer position, final Integer id, final String folderUuid,
                            final Integer importIndex, final String mappingName) {
        this.status = status;
        this.name = name;
        this.aemTitle = aemTitle;
        this.importStatus = importStatus;
        this.gcTemplateName = gcTemplateName;
        this.gcLink = gcLink;
        this.aemLink = aemLink;
        this.color = color;
        this.position = position;
        this.id = id;
        this.folderUuid = folderUuid;
        this.importIndex = importIndex;
        this.mappingName = mappingName;
    }

    /**
     * @return Live status in GatherContent.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status New Live status in GatherContent to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setStatus(final String status) {
        this.status = status;
        return this;
    }

    /**
     * @return Page title in AEM.
     */
    public String getAemTitle() {
        return aemTitle;
    }

    /**
     * @param aemTitle Page title in AEM to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setAemTitle(final String aemTitle) {
        this.aemTitle = aemTitle;
        return this;
    }

    /**
     * @return Item name in GatherContent.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name New Item name in GatherContent to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @return Import status - 'Imported' or 'Not-imported'.
     */
    public String getImportStatus() {
        return importStatus;
    }

    /**
     * @param importStatus New Import status to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setImportStatus(final String importStatus) {
        this.importStatus = importStatus;
        return this;
    }

    /**
     * @return Template name in GatherContent.
     */
    public String getGcTemplateName() {
        return gcTemplateName;
    }

    /**
     * @param gcTemplateName New Template name in GatherContent to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setGcTemplateName(final String gcTemplateName) {
        this.gcTemplateName = gcTemplateName;
        return this;
    }

    /**
     * @return Link to item in GatherContent.
     */
    public String getGcLink() {
        return gcLink;
    }

    /**
     * @param gcLink New Link to item in GatherContent to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setGcLink(final String gcLink) {
        this.gcLink = gcLink;
        return this;
    }

    /**
     * @return Link to page in AEM.
     */
    public String getAemLink() {
        return aemLink;
    }

    /**
     * @param aemLink New Link to page in AEM to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setAemLink(final String aemLink) {
        this.aemLink = aemLink;
        return this;
    }

    /**
     * @return Color of Live status in GatherContent.
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color Color of Live status in GatherContent to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setColor(final String color) {
        this.color = color;
        return this;
    }

    /**
     * @return Position in GatherContent list of items.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * @param position New Position in GatherContent list of items to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setPosition(Integer position) {
        this.position = position;
        return this;
    }

    /**
     * @inheritDoc
     * @return
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @param id New GatherContent Item ID to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setId(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * @inheritDoc
     */
    @Override
    @JsonProperty("folder_uuid")
    public String getFolderUuid() {
        return folderUuid;
    }

    /**
     * @param folderUuid New GatherContent Item ID of parent item to set.
     * @return ImportResultItem himself.
     */
    @JsonProperty("folder_uuid")
    public ImportResultItem setFolderUuid(String folderUuid) {
        this.folderUuid = folderUuid;
        return this;
    }

    /**
     * @param type type of AEM export resource to set.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @return type of Resource
     * @inheritDoc
     */
    public String getType() {
        return type;
    }

    /**
     * @return Index number as it came to be processed.
     */
    public Integer getImportIndex() {
        return importIndex;
    }

    /**
     * @param importIndex Index number as it came to be processed.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setImportIndex(Integer importIndex) {
        this.importIndex = importIndex;
        return this;
    }

    /**
     * @return Mapping Name.
     */
    public String getMappingName() {
        return mappingName;
    }

    /**
     * @param mappingName Mapping Name.
     * @return ImportResultItem himself.
     */
    public ImportResultItem setMappingName(String mappingName) {
        this.mappingName = mappingName;
        return this;
    }

    @Override
    public String toString() {
        return "ImportResultItem{"
            + "status='" + status + '\''
            + ", name='" + name + '\''
            + ", aemTitle='" + aemTitle + '\''
            + ", importStatus='" + importStatus + '\''
            + ", gcTemplateName='" + gcTemplateName + '\''
            + ", gcLink='" + gcLink + '\''
            + ", aemLink='" + aemLink + '\''
            + ", color='" + color + '\''
            + ", position=" + position
            + ", id='" + id + '\''
            + ", folderUuid='" + folderUuid + '\''
            + ", type='" + type + '\''
            + ", importIndex=" + importIndex
            + '}';
    }
}
