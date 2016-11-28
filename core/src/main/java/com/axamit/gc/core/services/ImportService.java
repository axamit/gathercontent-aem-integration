/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.sightly.models.ImportModel;

/**
 * The <tt>ImportService</tt> interface provides methods to get and update status of specific import/update process job.
 */
public interface ImportService {

    /**
     * Update status of import/update process and record it in JCR repository.
     *
     * @param pagePath     JCR path to cloud configuration page.
     * @param importStatus Information about current process status.
     * @throws GCException If any error occurs.
     */
    void updateStatus(String pagePath, ImportModel importStatus) throws GCException;

    /**
     * Get status of import/update process which is recorded in JCR repository.
     *
     * @param pagePath JCR path to cloud configuration page.
     * @param jobId    ID of sling job which execute import process.
     * @return Information about current process status.
     * @throws GCException If any error occurs.
     */

    ImportModel getImportStatus(String pagePath, String jobId) throws GCException;

}
