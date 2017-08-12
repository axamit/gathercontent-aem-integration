/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.axamit.gc.core.pojo.helpers.GCHierarchySortable;

/**
 * The <code>ImportUpdateTableItem</code> is a POJO class represent item in tables on import and update pages.
 *
 * @author Axamit, gc.support@axamit.com
 */
public final class ImportUpdateTableItem implements GCHierarchySortable {
    private String id;
    private String parentId;
    private String title;
    private String hierarchyTitle;
    private String status;
    private String gcTemplate;
    private String mappingName;
    private String mappingPath;
    private String importPath;
    private String gcPath;
    private String aemUpdateDate;
    private String gcUpdateDate;
    private String color;
    private String jsonInformation;
    private String validName;

    /**
     * @return ID of GatherContent item.
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return ID of GatherContent parent item.
     */
    public String getParentId() {
        return parentId;
    }

    public void setParentId(final String parentId) {
        this.parentId = parentId;
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
     * @return The title of item in GatherContent including '&lt;' markers of content hierarchy.
     */
    public String getHierarchyTitle() {
        return hierarchyTitle;
    }

    public void setHierarchyTitle(final String hierarchyTitle) {
        this.hierarchyTitle = hierarchyTitle;
    }

    /**
     * @return Live status name in GatherContent.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * @return GatherContent template name.
     */
    public String getGcTemplate() {
        return gcTemplate;
    }

    public void setGcTemplate(final String gcTemplate) {
        this.gcTemplate = gcTemplate;
    }

    /**
     * @return Mapping name.
     */
    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
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
     * @return Target JCR path for import items in AEM like '/content/geometrixx/en/targetforimport'.
     */
    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(final String importPath) {
        this.importPath = importPath;
    }

    /**
     * @return Last update date of page in AEM.
     */
    public String getAemUpdateDate() {
        return aemUpdateDate;
    }

    public void setAemUpdateDate(final String aemUpdateDate) {
        this.aemUpdateDate = aemUpdateDate;
    }

    /**
     * @return Last update date of item in GatherContent.
     */
    public String getGcUpdateDate() {
        return gcUpdateDate;
    }

    public void setGcUpdateDate(final String gcUpdateDate) {
        this.gcUpdateDate = gcUpdateDate;
    }

    /**
     * @return Live status color in GatherContent.
     */
    public String getColor() {
        return color;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public String getJsonInformation() {
        return jsonInformation;
    }

    public void setJsonInformation(final String jsonInformation) {
        this.jsonInformation = jsonInformation;
    }

    public String getValidName() {
        return validName;
    }

    public void setValidName(final String validName) {
        this.validName = validName;
    }

    public String getGcPath() {
        return gcPath;
    }

    public void setGcPath(final String gcPath) {
        this.gcPath = gcPath;
    }

    @Override
    public String toString() {
        return "ImportUpdateTableItem{"
            + "id='" + id + '\''
            + ", parentId='" + parentId + '\''
            + ", title='" + title + '\''
            + ", hierarchyTitle='" + hierarchyTitle + '\''
            + ", status='" + status + '\''
            + ", gcTemplate='" + gcTemplate + '\''
            + ", mappingPath='" + mappingPath + '\''
            + ", importPath='" + importPath + '\''
            + ", gcPath='" + gcPath + '\''
            + ", aemUpdateDate='" + aemUpdateDate + '\''
            + ", gcUpdateDate='" + gcUpdateDate + '\''
            + ", color='" + color + '\''
            + ", jsonInformation='" + jsonInformation + '\''
            + ", validName='" + validName + '\''
            + '}';
    }
}
