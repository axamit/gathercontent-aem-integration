/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

/**
 * Created by dzianis.baburkin on 7/7/2016.
 */
public final class Constants {

    private Constants() {
    }

    /**
     * GatherContent template ID JCR property name.
     */
    public static final String GC_TEMPLATE_ID_PN = "templateId";
    /**
     * GatherContent template name JCR property name.
     */
    public static final String GC_TEMPLATE_NAME_PN = "templateName";
    /**
     * GatherContent project ID JCR property name.
     */
    public static final String GC_PROJECT_ID_PN = "projectId";
    /**
     * AEM 'template page' path JCR property name.
     */
    public static final String AEM_TEMPLATE_PATH_PN = "templatePath";
    /**
     * AEM target import path JCR property name.
     */
    public static final String AEM_IMPORT_PATH_PN = "importPath";
    /**
     * GatherContent-AEM Mapping name JCR property name.
     */
    public static final String MAPPING_NAME_PN = "mappingName";
    /**
     * GatherContent-AEM Mapping path JCR property name.
     */
    public static final String MAPPING_PATH_PARAM_NAME = "mappingPath";

    /**
     * Project ID selector.
     */
    public static final String PROJECT_ID_SELECTOR = GC_PROJECT_ID_PN + "-";

    /**
     * Default content import path in JCR.
     */
    public static final String DEFAULT_IMPORT_PATH = "/content/gathercontent";
    /**
     * Default asset import path in JCR.
     */
    public static final String DEFAULT_IMPORT_DAM_PATH = "/content/dam/gathercontent";
    /**
     * AEM Forms 'Checkbox Group' and 'Radio Group' property name for default values.
     */
    public static final String DEFAULT_SELECTION_PN = "defaultValue";

    /**
     * JCR property name to indicate AEM page which was imported from GatherContent.
     */
    public static final String GC_IMPORTED_PAGE_MARKER = "isGCImportedPage";
    /**
     * JCR property name of imported page from GatherContent contains GatherContent project ID.
     */
    public static final String GC_IMPORTED_PAGE_PROJECT_ID = "GCProjectId";
    /**
     * JCR property name of imported page from GatherContent contains GatherContent item ID.
     */
    public static final String GC_IMPORTED_PAGE_ITEM_ID = "GCItemId";
    /**
     * JCR property name of imported page from GatherContent contains JCR path to mapping.
     */
    public static final String GC_IMPORTED_PAGE_MAPPING_PATH = "GCMappingPath";

    /**
     * Date format used for output all dates across application.
     */
    public static final String OUTPUT_DATE_FORMAT = "dd/MM/yyyy hh:mm a";
    /**
     * Date format used for items in <a href="https://gathercontent.com/developers/items/get-items/">GatherContent</a>.
     */
    public static final String ITEM_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * Property for mapping GatherContent Item Name to AEM field (JCR property).
     */
    public static final String META_ITEM_NAME = "META_NAME";
    /**
     * Message for user in case of unexpected error during request processing.
     */
    public static final String UNEXPECTED_ERROR_STRING =
            "An unexpected error has occurred. Your request cannot be processed at this time.";

    public static final String NEVER = "Never";
}
