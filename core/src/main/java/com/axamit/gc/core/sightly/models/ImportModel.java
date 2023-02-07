/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportResultItem;
import com.axamit.gc.core.pojo.ImportStatResult;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sling model class which represents import process.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties({"historyPath"})
@Model(adaptables = Resource.class)
public final class ImportModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportModel.class);

    public static final String PROPERTY_JOB_ID = "jobId";
    public static final String PROPERTY_IMPORT_ID = "importId";
    public static final String PROPERTY_IMPORTED_PAGES_DATA = "importedPagesData";
    public static final String PROPERTY_TOTAL_PAGES_COUNT = "totalPagesCount";
    public static final String PROPERTY_IMPORTED_PAGES_COUNT = "importedPagesCount";
    public static final String PROPERTY_IMPORT_START_DATE = "importStartDate";
    public static final String PROPERTY_IMPORT_END_DATE = "importEndDate";
    public static final String PROPERTY_PROJECT_NAME = "projectName";
    public static final String PROPERTY_JOB_TYPE = "jobType";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_PROJECT_ID = "projectId";

    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_COMPLETED = "Completed";

    @Inject
    @Named(ImportModel.PROPERTY_JOB_ID)
    private String jobId;

    @Inject
    @Named(ImportModel.PROPERTY_IMPORT_ID)
    @Optional
    private String importId;

    @Inject
    @Named(ImportModel.PROPERTY_IMPORTED_PAGES_DATA)
    @Optional
    private String importedPagesData;

    @Inject
    @Named(ImportModel.PROPERTY_TOTAL_PAGES_COUNT)
    @Optional
    private int totalPagesCount;

    @Inject
    @Named(ImportModel.PROPERTY_IMPORTED_PAGES_COUNT)
    @Optional
    private int importedPagesCountInt;

    @Inject
    @Named(ImportModel.PROPERTY_IMPORT_START_DATE)
    @Optional
    private Calendar importEndDate;

    @Inject
    @Named(ImportModel.PROPERTY_IMPORT_END_DATE)
    @Optional
    private Calendar importStartDate;

    @Inject
    @Named(ImportModel.PROPERTY_PROJECT_NAME)
    @Optional
    private String projectName;

    @Inject
    @Named(ImportModel.PROPERTY_JOB_TYPE)
    @Optional
    private String jobType;

    @Inject
    @Named(ImportModel.PROPERTY_STATUS)
    @Optional
    private String status;

    @Inject
    @Named(ImportModel.PROPERTY_PROJECT_ID)
    @Optional
    private String projectId;

    private Resource resource;

    private final Comparator<ImportResultItem> gcOrderComparator = (o1, o2) -> ObjectUtils.compare(o1.getPosition(), o2.getPosition());

    private final Comparator<ImportResultItem> initialImportOrderComparator = (o1, o2) -> ObjectUtils.compare(o1.getImportIndex(), o2.getImportIndex());

    /**
     * Empty constructor.
     */
    public ImportModel() {
    }

    /**
     * Constructor with resource field initialization.
     *
     * @param resource org.apache.sling.api.resource.Resource resource.
     */
    public ImportModel(final Resource resource) {
        this.resource = resource;
    }

    private volatile AtomicInteger importedPagesCount = new AtomicInteger();

    /**
     * PostConstruct sling model initializing.
     */
    @PostConstruct
    public void init() {
        importedPagesCount = new AtomicInteger(importedPagesCountInt);
    }

    /**
     * Increment count of imported pages.
     */
    public void increment() {
        importedPagesCount.incrementAndGet();
    }

    /**
     * Increases count of imported pages by given value.
     *
     * @param delta the value to add
     */
    public void incrementOnValue(final int delta) {
        importedPagesCount.addAndGet(delta);
    }

    public int getTotalPagesCount() {
        return totalPagesCount;
    }

    public void setTotalPagesCount(final int totalPagesCount) {
        this.totalPagesCount = totalPagesCount;
    }

    public int getImportedPagesCount() {
        return importedPagesCount.get();
    }

    public void setImportedPagesCount(final int importedPagesCount) {
        this.importedPagesCount = new AtomicInteger(importedPagesCount);
    }

    public String getImportedPagesData() {
        return importedPagesData;
    }

    public void setImportedPagesData(final String importedPagesData) {
        this.importedPagesData = importedPagesData;
    }

    /**
     * @return Sling Job ID of import.
     */
    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    /**
     * @return <code>Calendar</code> of end time of import.
     */
    public Calendar getImportEndDate() {
        return importEndDate;
    }

    public void setImportEndDate(final Calendar importEndDate) {
        this.importEndDate = importEndDate;
    }

    /**
     * @return <code>Calendar</code> of start time of import.
     */
    public Calendar getImportStartDate() {
        return importStartDate;
    }

    public void setImportStartDate(final Calendar importStartDate) {
        this.importStartDate = importStartDate;
    }

    /**
     * @return String representation of end time of import formatted according to
     * start time of import formatted according to <code>{@link Constants#OUTPUT_DATE_FORMAT}</code>
     */
    public String getFormattedStartDate() {
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.OUTPUT_DATE_FORMAT);
        return importStartDate != null ? outputDateFormat.format(importStartDate.getTime()) : null;
    }

    /**
     * @return String representation of end time of import formatted according to
     * <code>{@link Constants#OUTPUT_DATE_FORMAT}</code>
     */
    public String getFormattedEndDate() {
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.OUTPUT_DATE_FORMAT);
        return importEndDate != null ? outputDateFormat.format(importEndDate.getTime()) : null;
    }

    /**
     * @return Project id.
     */
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    /**
     * @return Project name.
     */
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return Short unique ID of import.
     */
    public String getImportId() {
        return importId;
    }

    public void setImportId(final String importId) {
        this.importId = importId;
    }

    /**
     * @return Job Type like 'Import', 'Export', 'Import Update', 'Export Update'.
     */
    public String getJobType() {
        return jobType;
    }

    public void setJobType(final String jobType) {
        this.jobType = jobType;
    }

    /**
     * @return URL to single import.
     */
    public String getHistoryPath() {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page page = pageManager.getContainingPage(resource);
        return page.getPath() + ".jobs.html/" + jobId;
    }

    /**
     * @return Status of import.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Sort and set names according to hierarchy.
     *
     * @throws GCException If any error occurs during JSON transformations.
     */
    public void sort() throws GCException {
        if (jobType != null) {
            ImportStatResult importStatResult =
                    JSONUtil.fromJsonToObject(importedPagesData, ImportStatResult.class);
            List<ImportResultItem> importedPages = importStatResult.getImportedPages();
            switch (jobType) {
                case Constants.JOB_TYPE_IMPORT:
                case Constants.JOB_TYPE_IMPORT + Constants.JOB_TYPE_POSTFIX_UPDATE:
                    importedPages.sort(gcOrderComparator);
                    importStatResult.setImportedPages(importedPages);
                    setHierarchyNamesForGCItemNames(importedPages);
                    this.importedPagesData = JSONUtil.fromObjectToJsonString(importStatResult);
                    break;
                case Constants.JOB_TYPE_EXPORT:
                case Constants.JOB_TYPE_EXPORT + Constants.JOB_TYPE_POSTFIX_UPDATE:
                    importedPages.sort(initialImportOrderComparator);
                    setHierarchyNamesForGCItemNames(importedPages);
                    this.importedPagesData = JSONUtil.fromObjectToJsonString(importStatResult);
                    break;
                default:
                    break;
            }
        }
    }

    private void setHierarchyNamesForGCItemNames(List<ImportResultItem> importedPages) {
        for (ImportResultItem importResultItem : importedPages) {
            importResultItem.setName(GCUtil.getHierarchyName(importedPages, importResultItem.getFolderUuid(),
                    importResultItem.getName()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ImportModel that = (ImportModel) o;

        return new EqualsBuilder().append(getTotalPagesCount(), that.getTotalPagesCount()).append(importedPagesCountInt, that.importedPagesCountInt).append(getJobId(), that.getJobId()).append(getImportId(), that.getImportId()).append(getImportedPagesData(), that.getImportedPagesData()).append(getImportEndDate(), that.getImportEndDate()).append(getImportStartDate(), that.getImportStartDate()).append(getProjectName(), that.getProjectName()).append(getJobType(), that.getJobType()).append(getStatus(), that.getStatus()).append(getProjectId(), that.getProjectId()).append(resource, that.resource).append(gcOrderComparator, that.gcOrderComparator).append(initialImportOrderComparator, that.initialImportOrderComparator).append(getImportedPagesCount(), that.getImportedPagesCount()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getJobId()).append(getImportId()).append(getImportedPagesData()).append(getTotalPagesCount()).append(importedPagesCountInt).append(getImportEndDate()).append(getImportStartDate()).append(getProjectName()).append(getJobType()).append(getStatus()).append(getProjectId()).append(resource).append(gcOrderComparator).append(initialImportOrderComparator).append(getImportedPagesCount()).toHashCode();
    }

    /*
    private void setHierarchyNamesForAemTitles(List<ImportResultItem> importedPages) {
        for (ImportResultItem importResultItem : importedPages) {
            importResultItem.setAemTitle(getHierarchyAemTitle(importedPages,
                    GCStringUtil.getRelativeNodePathFromPropertyPath(importResultItem.getAemLink()),
                    importResultItem.getAemTitle()));
        }
    }

    private String getHierarchyAemTitle(final List<ImportResultItem> itemList, final String parentPath,
                                        final String name) {
        if (parentPath == null || name == null) {
            return name;
        }
        String result = name;
        for (ImportResultItem item : itemList) {
            if (parentPath.equals(item.getAemLink())) {
                result = Constants.NEXT_LEVEL_HIERARCHY_INDENT + result;
                result = getHierarchyAemTitle(itemList,
                        GCStringUtil.getRelativeNodePathFromPropertyPath(item.getAemLink()), result);
            }
        }
        return result;
    }
    */
}
