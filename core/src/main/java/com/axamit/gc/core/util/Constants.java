/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

/**
 * Class for constants used around whole application.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum Constants {
    /*INSTANCE*/;
    /**
     * AEM property name for imported radio group other option value.
     */
    public static final String OTHER_OPTION_PROPERTY_NAME = "other_option_value";

    public static final String GC_MAPPING_PATH = "mappingPath";

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
     * Mapping Type parameter name.
     */
    public static final String MAPPING_TYPE_PARAM_NAME = "mappingType";
    /**
     * Mapping Type JCR property name.
     */
    static final String MAPPING_TYPE_PN = "mappingTypeStr";

    public static final String GC_PLUGIN_CONFIG_PATH_PARAM_NAME = "pluginConfigPath";
    /**
     * AEM 'template page' path JCR property name.
     */
    public static final String AEM_TEMPLATE_PATH_PN = "templatePath";
    /**
     * Content path, used to limit templates search for abstract template.
     */
    public static final String AEM_ABSTRACT_TEMPLATE_LIMIT_PATH = "abstractTemplateLimitPath";
    /**
     * AEM target import path JCR property name.
     */
    static final String AEM_IMPORT_PATH_PN = "importPath";
    /**
     * GatherContent-AEM Mapping name JCR property name.
     */
    public static final String MAPPING_NAME_PN = "mappingName";

    public static final String EXPORT_OR_IMPORT_MAPPING_TYPE_PN = "type";

    public static final String MAPPING_MAPPER_STR = "mapperStr";
    /**
     * GatherContent-AEM Mapping path JCR property name.
     */
    static final String MAPPING_PATH_PARAM_NAME = "mappingPath";

    /**
     * Project ID selector.
     */
    public static final String PROJECT_ID_SELECTOR = GC_PROJECT_ID_PN + "-";

    /**
     * Mapping Type selector.
     */
    public static final String MAPPING_TYPE_SELECTOR = MAPPING_TYPE_PARAM_NAME + "-";

    public static final String MAPPED_ITEMS_SELECTOR = "mapped";

    public static final String PAGE_CREATOR_SUBSERVICE_NAME = "PageCreator";

    public static final String SLING_RESOURCE_TYPE_PROPERTY_NAME = "sling:resourceType";

    public static final String IMPORT_LIST_NN = "import-list";

    public static final String PLUGINS_CONFIG_LIST_NN = "config-list";
    public static final String PLUGINS_CONFIG_LIST_RESOURCETYPE = "gathercontent/components/content/plugin-config-list";
    public static final String DEFAULT_PLUGIN_CONFIG_NN = "config_default";
    public static final String PLUGIN_CONFIG_NAME_PN = "configurationName";
    public static final String PLUGIN_CONFIG_RESOURCETYPE = "gathercontent/components/content/plugin-config";
    public static final String PLUGINS_CONFIG_SELECTOR = "config-";

    public static final String MAPPING_LIST_RESOURCETYPE = "gathercontent/components/content/mapping-list";
    public static final String MAPPING_RESOURCETYPE = "gathercontent/components/content/mapping";
    public static final String MAPPING_EXPORT_RESOURCETYPE = "gathercontent/components/content/mapping-export";
    public static final String MAPPING_LIST_NN = "mapping-list";
    public static final String MAPPING_SELECTOR = "mapping-";
    public static final String MAPPING_TYPE_IMPORT = "import";
    public static final String MAPPING_TYPE_EXPORT = "export";
    public static final String MAPPING_EXPORT_SELECTOR = "mappingexport";
    public static final String MAPPING_IMPORT_SELECTOR = "mapping";
    public static final String MAPPINGS_IMPORT_TYPE_LABEL = "Import Mappings";
    public static final String MAPPINGS_EXPORT_TYPE_LABEL = "Export Mappings";

    public static final String IMPORT_LIST_RESOURCETYPE = "gathercontent/components/content/import-list";
    public static final String IMPORT_ITEM_RESOURCETYPE = "gathercontent/components/content/import-item";
    /**
     * Default content import path in JCR.
     */
    public static final String DEFAULT_IMPORT_PATH = "/content/gathercontent";
    /**
     * Default asset import path in JCR.
     */
    public static final String DEFAULT_IMPORT_DAM_PATH = "/content/dam/gathercontent";
    /**
     * JCR path to AEM page to be used for items with no template in GatherContent.
     */
    public static final String NO_TEMPLATE_PAGE_PATH =
            "/etc/cloudservices/gathercontent/notemplatepagefolder/notemplatepage";
    /**
     * AEM Forms 'Checkbox Group' and 'Radio Group' property name for default values.
     */
    public static final String DEFAULT_SELECTION_PN = "defaultValue";

    /**
     * JCR property name to indicate AEM page which was imported from GatherContent.
     */
    static final String GC_IMPORTED_PAGE_MARKER = "isGCImportedPage";
    /**
     * JCR property name to indicate AEM page which was exported to GatherContent.
     */
    public static final String GC_EXPORTED_PAGE_MARKER = "isGCExportedPage";
    /**
     * JCR property name of imported page from GatherContent contains GatherContent project ID.
     */
    public static final String GC_IMPORTED_PAGE_PROJECT_ID = "GCProjectId";
    /**
     * JCR property name of exported page to GatherContent contains GatherContent project ID.
     */
    public static final String GC_EXPORTED_PAGE_PROJECT_ID = "GCExportedProjectId";
    /**
     * JCR property name of imported page from GatherContent contains GatherContent item ID.
     */
    public static final String GC_IMPORTED_PAGE_ITEM_ID = "GCItemId";
    public static final String GC_EXPORTED_PAGES_MAP = "GCExportedPagesMap";
    /**
     * JCR property name of exported page to GatherContent contains GatherContent item ID.
     */
    public static final String GC_EXPORTED_PAGE_ITEM_ID = "GCExportedItemId";
    /**
     * JCR property name of imported page from GatherContent contains JCR path to mapping.
     */
    public static final String GC_IMPORTED_PAGE_MAPPING_PATH = "GCMappingPath";
    /**
     * JCR property name of exported page to GatherContent contains JCR path to mapping.
     */
    public static final String GC_EXPORTED_PAGE_MAPPING_PATH = "GCExportedMappingPath";

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

    public static final String UNAVAILABLE_TEMPLATE = "";

    public static final String JOB_TYPE_IMPORT = "Import";
    public static final String JOB_TYPE_EXPORT = "Export";
    public static final String JOB_TYPE_POSTFIX_UPDATE = " Update";

    public static final String NEXT_LEVEL_HIERARCHY_INDENT = "   ";

    public static final String JSON_PN_TEXT = "text";
    public static final String JSON_PN_VALUE = "value";
    public static final String JSON_PN_QTIP = "qtip";
}
