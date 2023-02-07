/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.jobs;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCStatus;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.api.services.GCContentNewApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportData;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;
import com.axamit.gc.core.pojo.ImportStatResult;
import com.axamit.gc.core.services.GCPageModifier;
import com.axamit.gc.core.services.ImportService;
import com.axamit.gc.core.services.AEMPageModifier;
import com.axamit.gc.core.services.ThreadsPoolProvider;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * OSGI service consumes Sling job with topic "com/axamit/gc/core/jobs/import" which performs parallel importing of
 * content in background.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Component
@Service(value = {JobConsumer.class})
@Property(name = JobConsumer.PROPERTY_TOPICS, value = ImportExportJobConsumer.JOB_TOPIC)
public final class ImportExportJobConsumer implements JobConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportExportJobConsumer.class);

    public static final String JOB_TOPIC = "com/axamit/gc/core/jobs/import";
    public static final String JOB_PARAM_STATUS_STORE = "statusstore";  // to store context status
    public static final String JOB_PARAM_DATA = "data";      // data
    public static final String JOB_PARAM_CONTEXT_USERNAME = "context_username"; //context bug with objects serialization
    public static final String JOB_PARAM_CONTEXT_KEY = "context_key"; //context bug with objects serialization
    public static final String JOB_PARAM_ACCOUNT_ID = "account_id"; // account id
    public static final String JOB_PARAM_UPDATE_FLAG = "update";
    public static final String EXPORT_TO_GC_FLAG = "gc"; // update flag
    private static final int STATUS_UPDATE_PERIODICITY = 10;
    private static final double MAX_PERCENTS = 100;
    private final Lock statusUpdateLock = new ReentrantLock();
    private static final Map<String, String> SYMBOL_MAP = ImmutableMap.<String, String>builder()
            .put("&amp;", "&")
            .put("&lt;", "<")
            .put("&gt;", ">")
            .put("&quot;", "\"")
            .put("&#39;", "'")
            .put("&#x2F;", "/")
            .build();

    @Reference
    private AEMPageModifier aemPageModifier;

    @Reference
    private GCPageModifier gcPageModifier;

    @Reference
    private ImportService importService;

    @Reference
    private GCContentApi gcContentApi;

    @Reference
    private GCContentNewApi gcContentNewApi;

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
        final String data = (String) job.getProperty(JOB_PARAM_DATA);
        final String username = (String) job.getProperty(JOB_PARAM_CONTEXT_USERNAME);
        final String apiKey = (String) job.getProperty(JOB_PARAM_CONTEXT_KEY);
        final GCContext gcContext = GCContext.build(username, apiKey);
        final String statusStore = (String) job.getProperty(JOB_PARAM_STATUS_STORE);
        final int accountId = ((Long) job.getProperty(JOB_PARAM_ACCOUNT_ID)).intValue();
        final Boolean isUpdate = job.getProperty(JOB_PARAM_UPDATE_FLAG, false);
        final Boolean isExportToGC = job.getProperty(EXPORT_TO_GC_FLAG, false);

        if (accountId == 0) {
            LOGGER.error("Account id is 0");
            return JobResult.FAILED;
        }

        final List<GCAccount> gcAccounts = gcContentApi.accounts(gcContext);
        final String slug = gcAccounts.stream()
                .filter(gcAccount -> gcAccount.getId().equals(accountId))
                .findAny()
                .map(GCAccount::getSlug)
                .orElse(null);
        if (slug == null) {
            LOGGER.error("No corresponding domain is found for account id = " + accountId);
        }

        final ImportData importData = JSONUtil.fromJsonToObject(data, ImportData.class);
        final Integer projectId = importData.getProjectId();
        final List<ImportItem> importItemList = Collections.synchronizedList(importData.getItems());
        encodeImportItemTitle(importItemList);

        final GCStatus newStatusData = new GCStatus();
        newStatusData.setId(importData.getNewStatusId());
        newStatusData.setColor(importData.getNewStatusColor());
        newStatusData.setDisplayName(importData.getNewStatusName());

        importItemList.forEach(importItem -> {
            int index = importItemList.indexOf(importItem);
            importItem.setImportIndex(index);
            importItem.setSlug(slug);
            importItem.setNewStatusData(newStatusData);
            if (importItem.getImportPath() == null || importItem.getImportPath().isEmpty()) {
                importItem.setImportPath(Constants.DEFAULT_IMPORT_PATH);
            }
        });

        final ImportModel importModel = new ImportModel();
        importModel.setJobId(job.getId());
        importModel.setProjectName(importData.getProjectName());
        importModel.setTotalPagesCount(importItemList.size());
        importModel.setImportStartDate(Calendar.getInstance());
        importModel.setImportId(Long.toString(Calendar.getInstance().getTimeInMillis()));
        importModel.setJobType(GCUtil.getJobType(isUpdate, isExportToGC));

        if (Boolean.TRUE.equals(!isUpdate) && Boolean.TRUE.equals(!isExportToGC)) {
            final Map<String, Integer> mapPageCount = new ConcurrentHashMap<>();
            importItemList.parallelStream().forEach(importItem -> createPagesAndSetImportPaths(importItem, mapPageCount));
        }

        final ImportStatResult importStatResult = new ImportStatResult(Collections.synchronizedList(new ArrayList<>()));
        if (Boolean.TRUE.equals(!isUpdate) && Boolean.TRUE.equals(isExportToGC)) {
            exportHierarchically(importItemList, gcContext, importModel, importStatResult, statusStore, projectId);
        }

        List<Callable<Void>> callableList = Collections.synchronizedList(new ArrayList<>());
        if (!(isExportToGC && !isUpdate)) {
            callableList =  importItemList.stream().parallel().<Callable<Void>>map(importItem -> () -> {
                ImportResultItem gcPage = Boolean.TRUE.equals(isExportToGC)
                        ? gcPageModifier.updatePage(gcContext, importItem)
                        : aemPageModifier.updatePage(gcContext, importItem);
                importModel.increment();
                importStatResult.getImportedPages().add(gcPage);
                setImportModelStatus(importModel, importStatResult);
                updateStatus(importStatResult, statusStore, importModel);
                return null;
            }).collect(Collectors.toList());
        }
        try {
            poolProvider.invokeAll(callableList);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            return JobResult.FAILED;
        }
        importModel.setImportEndDate(Calendar.getInstance());
        setImportModelStatus(importModel, importStatResult);
        updateStatus(importStatResult, statusStore, importModel);
        return JobResult.OK; // Not relevant for background threads.
    }

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

    private void exportHierarchically(final List<ImportItem> importItemList,
                                      final GCContext gcContext,
                                      final ImportModel importModel, final ImportStatResult importStatResult,
                                      final String statusStore, final Integer projectId) {

        while (!importItemList.isEmpty()) {
            //get one level items
            Collection<ImportItem> oneLevelItems;
            ImportItem importItem = importItemList.get(0);
            oneLevelItems = importItemList.stream().filter(forItem -> forItem.getGcTargetItemName() != null && importItem.getGcTargetItemName() != null
                    && forItem.getImportPath() != null && importItem.getImportPath() != null
                    && forItem.getGcTargetItemName().equals(importItem.getGcTargetItemName())
                    && StringUtils.countMatches(forItem.getImportPath(), "/") == StringUtils.
                    countMatches(importItem.getImportPath(), "/")).collect(Collectors.toList());

            //get merge items groups
            Collection<List<ImportItem>> duplicatedAfterMergeItems = oneLevelItems.stream()
                    .map(item -> findExportItemsToMerge(item, importItemList))
                    .collect(Collectors.toSet());

            //get rest items, without current one level items
            importItemList.removeAll(oneLevelItems);

            //export one level items

            Collection<Callable<Void>> callableList = Collections.synchronizedList(new ArrayList<>());
            duplicatedAfterMergeItems.forEach(itemsToMerge -> {
                final List<ImportItem> childrenItems = itemsToMerge.stream()
                        .flatMap(itemToMerge -> findChildrenExportItems(itemToMerge, importItemList)
                        .stream())
                        .collect(Collectors.toList());
                callableList.add(() -> {
                    List<ImportResultItem> gcPages = gcPageModifier.createPage(itemsToMerge, gcContext, childrenItems, projectId);
                    importModel.incrementOnValue(gcPages.size());
                    importStatResult.getImportedPages().addAll(gcPages);
                    updateStatus(importStatResult, statusStore, importModel);
                    return null;
                });
            });
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

    private void createPagesAndSetImportPaths(final ImportItem importItem, final Map<String, Integer> mapPageCount) {
        Page createdPage = aemPageModifier.createPage(importItem, mapPageCount);
        importItem.setImportPath(createdPage != null ? createdPage.getPath() : null);
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

    private void encodeImportItemTitle(final List<ImportItem> items) {
        items.forEach(item -> item.setTitle(SYMBOL_MAP.entrySet().stream()
                .reduce(item.getTitle(), (title, entry) -> title.replaceAll(entry.getKey(), entry.getValue()), (s1, s2) -> s1)));
    }
}
