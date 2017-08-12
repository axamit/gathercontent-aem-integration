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
import com.axamit.gc.core.services.GCItemCreator;
import com.axamit.gc.core.services.ImportService;
import com.axamit.gc.core.services.PageCreator;
import com.axamit.gc.core.services.ThreadsPoolProvider;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OSGI service consumes Sling job with topic "com/axamit/gc/core/jobs/import" which performs parallel importing of
 * content in background.
 *
 * @author Axamit, gc.support@axamit.com
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
    public static final String JOB_PARAM_UPDATE_FLAG = "update";
    public static final String IMPORT_SIDE_UPDATE_FLAG = "gc"; // update flag
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportJobConsumer.class);
    private static final int STATUS_UPDATE_PERIODICITY = 10;
    private static final double MAX_PERCENTS = 100;
    private final Lock statusUpdateLock = new ReentrantLock();
    @Reference
    private PageCreator pageCreator;
    @Reference
    private GCItemCreator gcItemCreator;
    @Reference
    private ImportService importService;
    @Reference
    private GCContentApi gcContentApi;
    @Reference
    private ThreadsPoolProvider poolProvider;

    private static void setImportModelStatus(final ImportModel importModel, final ImportStatResult importStatResult) {
        if (importStatResult.getFailedNumber() > 0) {
            importModel.setStatus(ImportModel.STATUS_FAILED);
        } else {
            if (importModel.getImportedPagesCount() == importModel.getTotalPagesCount()) {
                importModel.setStatus(ImportModel.STATUS_COMPLETED);
            } else {
                importModel.setStatus("In progress, "
                    + Math.round((double) importModel.getImportedPagesCount()
                    / (double) importModel.getTotalPagesCount() * MAX_PERCENTS) + '%');
            }
        }
    }

    private static List<ImportItem> findExportItemsToMerge(final ImportItem sampleItem,
                                                           final Iterable<ImportItem> importItemList) {
        ImmutableList.Builder<ImportItem> resultItemList = ImmutableList.builder();
        for (ImportItem importItem : importItemList) {
            if (importItem.getGcTargetItemName() != null && sampleItem.getGcTargetItemName() != null
                && importItem.getTitle() != null && sampleItem.getTitle() != null
                && importItem.getGcTargetItemId() != null && sampleItem.getGcTargetItemId() != null
                && importItem.getGcTargetItemName().equals(sampleItem.getGcTargetItemName())
                && importItem.getTitle().equals(sampleItem.getTitle())
                && importItem.getGcTargetItemId().equals(sampleItem.getGcTargetItemId())) {
                resultItemList.add(importItem);
                LOGGER.info("Export: importItem - {}, found item to merge - {}", sampleItem.getImportPath(),
                    importItem.getImportPath());
            }
        }
        return resultItemList.build();
    }

    private static Collection<ImportItem> findChildrenExportItems(final ImportItem sampleItem,
                                                                  final Iterable<ImportItem> importItemList) {
        ImmutableList.Builder<ImportItem> resultItemList = ImmutableList.builder();
        if (sampleItem.getImportPath() != null) {
            for (ImportItem importItem : importItemList) {
                if (sampleItem.getImportPath() != null && sampleItem.getImportPath().equals(
                    importItem.getImportPath().substring(0, importItem.getImportPath().lastIndexOf("/")))) {
                    resultItemList.add(importItem);
                    LOGGER.info("Export: importItem - {}, found children item - {}", sampleItem.getImportPath(),
                        importItem.getImportPath());
                }
            }
        }
        return resultItemList.build();
    }

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
        String data = (String) job.getProperty(JOB_PARAM_DATA);
        String username = (String) job.getProperty(JOB_PARAM_CONTEXT_USERNAME);
        String apiKey = (String) job.getProperty(JOB_PARAM_CONTEXT_KEY);
        final GCContext gcContext = GCContext.build(username, apiKey);
        final String statusStore = (String) job.getProperty(JOB_PARAM_STATUS_STORE);
        String accountId = (String) job.getProperty(JOB_PARAM_ACCOUNT_ID);
        Boolean isUpdate = job.getProperty(JOB_PARAM_UPDATE_FLAG, false);
        final Boolean isImportInGC = job.getProperty(IMPORT_SIDE_UPDATE_FLAG, false);

        if (accountId == null) {
            LOGGER.error("Account id is null");
            return JobResult.FAILED;
        }
        List<GCAccount> gcAccounts = gcContentApi.accounts(gcContext);

        String slug = null;
        for (GCAccount gcAccount : gcAccounts) {
            if (gcAccount.getId().equals(accountId)) {
                slug = gcAccount.getSlug();
            }
        }

        if (slug == null) {
            LOGGER.error("No corresponding domain is found for account id = " + accountId);
        }

        ImportData importData = JSONUtil.fromJsonToObject(data, ImportData.class);
        final String projectId = importData.getProjectId();
        final List<ImportItem> importItemList = Collections.synchronizedList(importData.getItems());
        final GCData newStatusData = new GCData();
        newStatusData.setId(importData.getNewStatusId());
        newStatusData.setColor(importData.getNewStatusColor());
        newStatusData.setName(importData.getNewStatusName());
        for (int index = 0; index < importItemList.size(); index++) {
            ImportItem importItem = importItemList.get(index);
            importItem.setImportIndex(index);
            importItem.setSlug(slug);
            importItem.setNewStatusData(newStatusData);
            if (importItem.getImportPath() == null || importItem.getImportPath().isEmpty()) {
                importItem.setImportPath(Constants.DEFAULT_IMPORT_PATH);
            }
        }
        final ImportModel importModel = new ImportModel();
        importModel.setJobId(job.getId());
        importModel.setProjectName(importData.getProjectName());
        importModel.setTotalPagesCount(importItemList.size());
        final ImportStatResult importStatResult =
            new ImportStatResult(Collections.synchronizedList(new ArrayList<ImportResultItem>()));


        importModel.setImportStartDate(Calendar.getInstance());
        importModel.setImportId(Long.toString(Calendar.getInstance().getTimeInMillis()));
        importModel.setJobType(GCUtil.getJobType(isUpdate, isImportInGC));
        if (!isUpdate && !isImportInGC) {
            final Map<String, Integer> mapPageCount = new HashMap<>();
            for (final ImportItem importItem : GCUtil.reorderToTree(importItemList)) {
                importHierarchically(importItem, mapPageCount);
            }
        }
        if (!isUpdate && isImportInGC) {
            exportHierarchically(importItemList, gcContext, importModel, importStatResult,
                statusStore, projectId);
        }
        ImmutableList.Builder<Callable<Void>> callableList = ImmutableList.builder();
        if (!(isImportInGC && !isUpdate)) {
            for (final ImportItem importItem : importItemList) {
                callableList.add(new Callable<Void>() {
                    @Override
                    public Void call() {
                        ImportResultItem gcPage = isImportInGC ? gcItemCreator.updateGCPage(importItem, gcContext)
                            : pageCreator.updateGCPage(gcContext, importItem);
                        importModel.increment();
                        importStatResult.getImportedPages().add(gcPage);
                        setImportModelStatus(importModel, importStatResult);
                        updateStatus(importStatResult, statusStore, importModel);
                        return null;
                    }
                });
            }
        }
        try {
            poolProvider.invokeAll(callableList.build());
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return JobResult.FAILED;
        }
        importModel.setImportEndDate(Calendar.getInstance());
        setImportModelStatus(importModel, importStatResult);
        updateStatus(importStatResult, statusStore, importModel);
        return JobResult.OK; // Not relevant for background threads.
    }

    private void exportHierarchically(final List<ImportItem> importItemList,
                                      final GCContext gcContext,
                                      final ImportModel importModel, final ImportStatResult importStatResult,
                                      final String statusStore, final String projectId) {

        while (!importItemList.isEmpty()) {
            //get one level items
            Collection<ImportItem> oneLevelItems = new ArrayList<>();
            ImportItem importItem = importItemList.get(0);
            for (ImportItem forItem : importItemList) {
                if (forItem.getGcTargetItemName() != null && importItem.getGcTargetItemName() != null
                    && forItem.getImportPath() != null && importItem.getImportPath() != null
                    && forItem.getGcTargetItemName().equals(importItem.getGcTargetItemName())
                    && StringUtils.countMatches(forItem.getImportPath(), "/") == StringUtils.
                    countMatches(importItem.getImportPath(), "/")) {
                    oneLevelItems.add(forItem);
                }
            }

            //get merge items groups
            Collection<List<ImportItem>> duplicatedAfterMergeItems = new HashSet<>();

            for (ImportItem item : oneLevelItems) {
                final List<ImportItem> itemsToMerge = findExportItemsToMerge(item, importItemList);
                duplicatedAfterMergeItems.add(itemsToMerge);
            }

            //get rest items, without current one level items
            importItemList.removeAll(oneLevelItems);

            //export one level items

            Collection<Callable<Void>> callableList = new ArrayList<>();
            for (final List<ImportItem> itemsToMerge : duplicatedAfterMergeItems) {
                final List<ImportItem> childrenItems = new ArrayList<>();
                for (ImportItem itemToMerge : itemsToMerge) {
                    childrenItems.addAll(findChildrenExportItems(itemToMerge, importItemList));
                }
                callableList.add(
                    new Callable<Void>() {
                        @Override
                        public Void call() {
                            List<ImportResultItem> gcPages =
                                gcItemCreator.createGCPage(itemsToMerge, gcContext, childrenItems, projectId);
                            importModel.incrementOnValue(gcPages.size());
                            importStatResult.getImportedPages().addAll(gcPages);
                            updateStatus(importStatResult, statusStore, importModel);
                            return null;
                        }
                    });
            }
            try {
                waitForCallableList(callableList);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void waitForCallableList(Collection<Callable<Void>> callableList) throws InterruptedException {
        List<Future<Void>> result = poolProvider.invokeAll(callableList);
        for (Future<Void> future : result) {
            try {
                future.get();
            } catch (ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void importHierarchically(final ImportItem importItem, final Map<String, Integer> mapPageCount) {
        Page createdPage = pageCreator.createGCPage(importItem, mapPageCount);
        if (createdPage != null) {
            importItem.setImportPath(createdPage.getPath());
            for (ImportItem innerImportItem : importItem.getChildren()) {
                innerImportItem.setImportPath(createdPage.getPath());
                GCUtil.rewriteChildrenImportPaths(innerImportItem);
            }
        } else {
            importItem.setImportPath(null);
        }
        if (!importItem.getChildren().isEmpty()) {
            for (final ImportItem innerImportItem : importItem.getChildren()) {
                LOGGER.info("Import a child item {}: {}", innerImportItem.getItemId(), innerImportItem.getTitle());
                importHierarchically(innerImportItem, mapPageCount);
            }
        }
    }

    private void updateStatus(final ImportStatResult importStatResult, final String pagePath,
                              final ImportModel importModel) {
        final int importedPagesCount = importModel.getImportedPagesCount();
        if (importedPagesCount == importModel.getTotalPagesCount()) {
            statusUpdateLock.lock();
            performStatusUpdate(importStatResult, pagePath, importModel);
        } else if (importedPagesCount % STATUS_UPDATE_PERIODICITY == 0 && statusUpdateLock.tryLock()) {
            performStatusUpdate(importStatResult, pagePath, importModel);
        }
    }

    private void performStatusUpdate(ImportStatResult importStatResult, String pagePath, ImportModel importModel) {
        try {
            importModel.setImportedPagesData(JSONUtil.fromObjectToJsonString(importStatResult));
            importService.updateStatus(pagePath, importModel);
        } catch (GCException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            statusUpdateLock.unlock();
        }
    }
}
