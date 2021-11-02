/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.axamit.gc.api.dto.GCStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The <code>ImportItem</code> represents information about item need to be imported or exported.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportItem {
    private Integer itemId;
    private String mappingPath;
    private String folderUuid;
    private String importPath;
    private String title;
    private String template;
    private Integer gcTemplateId;
    private List<ImportItem> children = new ArrayList<>();
    private String slug; //! May items have different slugs?
    private GCStatus newStatusData;
    private String gcTargetItemName = "";
    private Integer gcTargetItemId = 0;
    private Boolean isProceed = false;
    private String aemTitle;
    private Integer importIndex;
    private String mappingName;

    /**
     * @return Index number as it came to be processed.
     */
    public Integer getImportIndex() {
        return importIndex;
    }

    public void setImportIndex(Integer importIndex) {
        this.importIndex = importIndex;
    }

    /**
     * @return ID of GatherContent item.
     */
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(final Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * @return JCR path to mapping in AEM like
     * '/etc/cloudservices/gathercontent/gathercontent-importer/jcr:content/mapping-list/mapping_1'.
     */
    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(final String mappingPath) {
        this.mappingPath = mappingPath;
    }

    /**
     * @return ID of GatherContent parent item.
     */
    @JsonProperty("folder_uuid")
    public String getFolderUuid() {
        return folderUuid;
    }

    @JsonProperty("folder_uuid")
    public void setFolderUuid(final String folderUuid) {
        this.folderUuid = folderUuid;
    }

    /**
     * @return Target JCR path for import items in AEM like '/content/geometrixx/en/targetforimport'.
     */
    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(final String importPath) {
        this.importPath = importPath;
    }

    /**
     * @return The title of item in GatherContent.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * @return GatherContent template name.
     */
    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    /**
     * @return List of children items.
     */
    public List<ImportItem> getChildren() {
        return children;
    }

    public void setChildren(final List<ImportItem> children) {
        this.children = children;
    }

    /**
     * @return Subdomain of account on GatherContent.
     */
    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    /**
     * @return New live status data to be applied on an GatherContent item.
     */
    public GCStatus getNewStatusData() {
        return newStatusData;
    }

    public void setNewStatusData(final GCStatus newStatusData) {
        this.newStatusData = newStatusData;
    }

    /**
     * @return Name of item in GatherContent which will act as parent item for export.
     */
    public String getGcTargetItemName() {
        return gcTargetItemName;
    }

    public void setGcTargetItemName(final String gcTargetItemName) {
        this.gcTargetItemName = gcTargetItemName;
    }

    /**
     * @return ID of item in GatherContent which will act as parent item for export.
     */
    public Integer getGcTargetItemId() {
        return gcTargetItemId;
    }

    public void setGcTargetItemId(final Integer gcTargetItemId) {
        this.gcTargetItemId = gcTargetItemId;
    }

    /**
     * @return ID of GatherContent template.
     */
    public Integer getGcTemplateId() {
        return gcTemplateId;
    }

    public void setGcTemplateId(final Integer gcTemplateId) {
        this.gcTemplateId = gcTemplateId;
    }

    /**
     * @return AEM title for export.
     */
    public String getAemTitle() {
        return aemTitle;
    }

    public void setAemTitle(final String aemTitle) {
        this.aemTitle = aemTitle;
    }

    /**
     * @return True if item was already proceed, false otherwise.
     */
    public Boolean getProceed() {
        return isProceed;
    }

    public void setProceed(final Boolean proceed) {
        isProceed = proceed;
    }

    /**
     * @return Mapping Name.
     */
    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImportItem that = (ImportItem) o;

        return Objects.equals(importPath, that.importPath);

    }

    @Override
    public int hashCode() {
        return importPath != null ? importPath.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ImportItem{"
                + "itemId='" + itemId + '\''
                + ", title='" + title + '\''
                + '}';
    }
}
