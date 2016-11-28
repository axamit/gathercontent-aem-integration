/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>ImportStatResult</code> represents statistics about import process.
 */
public final class ImportStatResult {
    private List<ImportResultItem> importedPages;
    private int importedNumber;
    private int failedNumber;

    /**
     * Public constructor.
     *
     * @param importedPages  List of results of import.
     * @param importedNumber Number of successfully imported pages.
     * @param failedNumber   Number of pages imported with failure.
     */
    public ImportStatResult(final List<ImportResultItem> importedPages, final int importedNumber,
                            final int failedNumber) {
        this.importedPages = importedPages;
        this.importedNumber = importedNumber;
        this.failedNumber = failedNumber;
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

        importedNumber = newImportedNumber;
        return importedNumber;
    }

    public void setImportedNumber(final int importedNumber) {
        this.importedNumber = importedNumber;
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

        failedNumber = newFailedNumber;
        return failedNumber;
    }

    private ArrayList<ImportResultItem> copyAndGetPages() {
        return new ArrayList<>(importedPages);
    }

    public void setFailedNumber(final int failedNumber) {
        this.failedNumber = failedNumber;
    }
}
