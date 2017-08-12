/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * The <code>ImportStatResult</code> represents statistics about import process.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ImportStatResult {
    private List<ImportResultItem> importedPages;

    //! Possible regression

    /**
     * Public constructor.
     *
     * @param importedPages  List of results of import.
     */
    @JsonCreator
    public ImportStatResult(@JsonProperty("importedPages") final List<ImportResultItem> importedPages) {
        this.importedPages = importedPages;
    }

    /**
     * @return <tt>List</tt> of results of import.
     */
    public List<ImportResultItem> getImportedPages() {
        return importedPages;
    }

    public void setImportedPages(final List<ImportResultItem> importedPages) {
        this.importedPages = importedPages;
    }

    /**
     * @return Number of successfully imported pages.
     */
    public int getImportedNumber() {
        return recalculateImportedNumber();
    }

    private int recalculateImportedNumber() {
        int newImportedNumber = 0;

        for (ImportResultItem importResultItem : copyAndGetPages()) {
            if (ImportResultItem.IMPORTED.equals(importResultItem.getImportStatus())) {
                newImportedNumber++;
            }
        }

        return newImportedNumber;
    }

    /**
     * @return Number of pages imported with failure.
     */
    public int getFailedNumber() {
        return recalculateFailedNumber();
    }

    private int recalculateFailedNumber() {
        int newFailedNumber = 0;

        for (ImportResultItem importResultItem : copyAndGetPages()) {
            if (!ImportResultItem.IMPORTED.equals(importResultItem.getImportStatus())) {
                newFailedNumber++;
            }
        }

        return newFailedNumber;
    }

    private Iterable<ImportResultItem> copyAndGetPages() {
        return ImmutableList.copyOf(importedPages);
    }

    @Override
    public String toString() {
        return "ImportStatResult{"
            + "importedPages=" + importedPages
            + '}';
    }
}
