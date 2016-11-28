/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.sightly.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sling model class which represents import process.
 */
@Model(adaptables = Resource.class)
public final class ImportModel {

    public static final String PROPERTY_JOB_ID = "jobId";
    public static final String PROPERTY_IMPORTED_PAGES_DATA = "importedPagesData";
    public static final String PROPERTY_TOTAL_PAGES_COUNT = "totalPagesCount";
    public static final String PROPERTY_IMPORTED_PAGES_COUNT = "importedPagesCount";

    @Inject
    @Named(ImportModel.PROPERTY_JOB_ID)
    private String jobId;

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

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }
}
