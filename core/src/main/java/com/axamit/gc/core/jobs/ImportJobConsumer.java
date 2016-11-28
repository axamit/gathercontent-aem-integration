/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.jobs;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCData;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportData;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;
import com.axamit.gc.core.pojo.ImportStatResult;
import com.axamit.gc.core.services.ImportService;
import com.axamit.gc.core.services.PageCreator;
import com.axamit.gc.core.services.ThreadsPoolProvider;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * OSGI service consumes Sling job with topic "com/axamit/gc/core/jobs/import" which performs parallel importing of
 * content in background.
 */
@Component
@Service(value = {JobConsumer.class})
@Property(name = JobConsumer.PROPERTY_TOPICS, value = ImportJobConsumer.JOB_TOPIC)
public final class ImportJobConsumer implements JobConsumer {

    public static final String JOB_TOPIC = "com/axamit/gc/core/jobs/import";

    public static final String JOB_PARAM_STATUS_STORE = "statusstore";  // to store context status
    public static final String JOB_PARAM_DATA = "data";      // data
    //    public static final String JOB_PARAM_CONTEXT = "context"; //context bug with objects serialization
    public static final String JOB_PARAM_CONTEXT_USERNAME = "context_username"; //context bug with objects serialization
    public static final String JOB_PARAM_CONTEXT_KEY = "context_key"; //context bug with objects serialization

    public static final String JOB_PARAM_ACCOUNT_ID = "account_id"; // account id
    public static final String JOB_PARAM_UPDATE_FLAG = "update";      // update flag
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportJobConsumer.class);
    @Reference
    private PageCreator pageCreator;
    @Reference
    private ImportService importService;
    @Reference
    private GCContentApi gcContentApi;
    @Reference
    private ThreadsPoolProvider poolProvider;

    @Override
    public JobResult process(final Job job) {
        try {
            return doJob(job);
        } catch (Exception e) {
            LOGGER.error("Failed import or update from GatherContent", e);
            return JobResult.FAILED;
        }
    }

    private JobResult doJob(final Job job) throws GCException {
        StopWatch sw = new StopWatch();
        sw.start();
        long prevTime = 0;
        String data = (String) job.getProperty(JOB_PARAM_DATA);
        String username = (String) job.getProperty(JOB_PARAM_CONTEXT_USERNAME);
        String apiKey = (String) job.getProperty(JOB_PARAM_CONTEXT_KEY);
        final GCContext gcContext = GCContext.build(username, apiKey);
        final String statusStore = (String) job.getProperty(JOB_PARAM_STATUS_STORE);
        String accountId = (String) job.getProperty(JOB_PARAM_ACCOUNT_ID);
        Boolean isUpdate = (Boolean) job.getProperty(JOB_PARAM_UPDATE_FLAG);

        String slug = null;

        if (accountId == null) {
            LOGGER.error("Account id is null");
        } else {
            List<GCAccount> gcAccounts = gcContentApi.accounts(gcContext);

            for (GCAccount gcAccount : gcAccounts) {
                if (gcAccount.getId().equals(accountId)) {
                    slug = gcAccount.getSlug();
                }
            }
        }

        if (slug == null) {
            LOGGER.error("No corresponding domain is found for account id = " + accountId);
        }

        ImportData importData = JSONUtil.fromJsonToObject(data, ImportData.class);
        List<ImportItem> importItemList = importData.getItems();
        final GCData newStatusData = new GCData();
        newStatusData.setId(importData.getNewStatusId());
        newStatusData.setColor(importData.getNewStatusColor());
        newStatusData.setName(importData.getNewStatusName());
        for (ImportItem importItem : importItemList) {
            importItem.setSlug(slug);
            importItem.setNewStatusData(newStatusData);
            if (importItem.getImportPath() == null) {
                importItem.setImportPath(Constants.DEFAULT_IMPORT_PATH);
            }
        }
        final ImportModel importModel = new ImportModel();
        importModel.setJobId(job.getId());
        importModel.setTotalPagesCount(importItemList.size());
        final ImportStatResult importStatResult =
                new ImportStatResult(Collections.synchronizedList(new ArrayList<ImportResultItem>()), 0, 0);
        final Map<String, Integer> mapPageCount = new HashMap<>();
        if (!isUpdate) {
            prevTime = sw.getTime();
            for (final ImportItem importItem : GCUtil.reorderToTree(importItemList)) {
                importHierarchically(importItem, mapPageCount);
            }
            LOGGER.debug("Execution time: Creating {} AEM pages - {} ms",
                    importItemList.size(), sw.getTime() - prevTime);
            prevTime = sw.getTime();
        }
        //List<Runnable> tasks = new ArrayList<>();
        List<Callable<Void>> callableList = new ArrayList<>();
        for (final ImportItem importItem : importItemList) {
            callableList.add(new Callable<Void>() {
                @Override
                public Void call() {
                    ImportResultItem gcPage = pageCreator.updateGCPage(gcContext, importItem);
                    importModel.increment();
                    importStatResult.getImportedPages().add(gcPage);
                    try {
                        updateStatus(importStatResult, statusStore, importModel);
                    } catch (GCException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                }
            });
        }
        try {
            poolProvider.invokeAll(callableList);
            LOGGER.debug("Execution time: Update of {} AEM pages - {} ms",
                    importItemList.size(), sw.getTime() - prevTime);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return JobResult.FAILED;
        }
        sw.stop();
        LOGGER.debug("Execution time: Full import of {} AEM pages - {} ms",
                importItemList.size(), sw.getTime());
        return JobResult.OK; // Not relevant for background threads.
    }


    private void importHierarchically(final ImportItem importItem, final Map<String, Integer> mapPageCount) {
        Page createdPage = pageCreator.createGCPage(importItem, mapPageCount);
        if (createdPage != null) {
            importItem.setImportPath(createdPage.getPath());
        } else {
            importItem.setImportPath(null);
        }
        if (!importItem.getChildren().isEmpty()) {
            for (final ImportItem innerImportItem : importItem.getChildren()) {
                LOGGER.debug("Import a child item {}: {}", innerImportItem.getItemId(), innerImportItem.getTitle());
                importHierarchically(innerImportItem, mapPageCount);
            }
        }
    }

    private void updateStatus(final ImportStatResult importStatResult, final String pagePath,
                              final ImportModel importModel)
            throws GCException {
        importModel.setImportedPagesData(JSONUtil.fromObjectToJsonString(importStatResult));
        try {
            importService.updateStatus(pagePath, importModel);
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
