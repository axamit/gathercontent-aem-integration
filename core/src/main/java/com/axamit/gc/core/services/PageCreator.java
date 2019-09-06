/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.pojo.ImportItem;
import com.axamit.gc.core.pojo.ImportResultItem;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.LoginException;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 * The <tt>PageCreator</tt> interface provides methods to create pages, assets and provide field mapping information,
 * which also needs access to JCR repository in AEM.
 *
 * @author Axamit, gc.support@axamit.com
 */
public interface PageCreator {

    /**
     * Creates a new Asset at the given path in AEM JCR repository. Remote URL is used as a source.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param parentPath The path of the asset to be created.
     * @param sourceURL  Remote URL of source.
     * @param mimetype   The mime type of the new asset's original binary.
     * @param doSave     Whether the repository changes are saved or not.
     * @return The newly created asset.
     */
    Asset createAsset(GCContext gcContext, String parentPath, String sourceURL, String mimetype, boolean doSave);

    /**
     * Update page that already exists in AEM. It used during update process. During new import process it should be
     * called after {@link #createGCPage(ImportItem, Map)}, which create pages in AEM at first, and after creating they
     * can be updated by this method, since it is not create new pages, just update exists.
     *
     * @param gcContext  <code>{@link GCContext}</code> object.
     * @param importItem <code>{@link ImportItem}</code> object with information about page need to be updated.
     * @return <code>{@link ImportResultItem}</code> with information about updated page.
     */
    ImportResultItem updateGCPage(GCContext gcContext, ImportItem importItem);

    /**
     * Create new page in AEM based on information of <code>{@link ImportItem}</code> about GatherContent page.
     * It used during new import process, and not during update process.
     *
     * @param importItem   <code>{@link ImportItem}</code> object with information about page need to be created.
     * @param mapPageCount Map with page names and number of those pages created during current import run. It used for
     *                     correct siblings naming. If there are several siblings have the same name in GC we need for
     *                     each of them generate valid page name using AEM best practice in naming.
     *                     e.g. <name>, <name>0, <name>1, etc. If pages with such name already exist (probably were
     *                     create with previous import) we need to update this page.
     * @return Created page in AEM.
     */
    Page createGCPage(ImportItem importItem, Map<String, Integer> mapPageCount);

    /**
     * Creates assets for all attachment fields of target GatherContent item.
     *
     * @param gcContext     <code>{@link GCContext}</code> object.
     * @param gcItem        GatherContent page item object.
     * @param importDAMPath JCR path (supposed be to path in DAM) to create new assets.
     * @return <tt>Map</tt> with GatherContent field as a key and he newly created assets as a value.
     * @throws LoginException If an error occurs during creating the new ResourceResolver for the service
     *                        represented by the calling bundle.
     * @throws GCException    If error occurred during receiving data from GatherContent.
     */
    Map<String, List<Asset>> createGCAssets(GCContext gcContext, GCItem gcItem, String importDAMPath)
            throws LoginException, GCException;

    /**
     * Builds collection of GatherContent field and accordingly AEM fields which could be mapped to this GatherContent
     * field.
     *
     * @param gcConfigs                 List of GatherContent configs.
     * @param useAbstract               Use abstract template for mapping combined from set of template pages.
     * @param addEmptyValue             Add empty mapping option to set of properties.
     * @param templatePath              JCR path in AEM to template page.
     * @param configurationPath         JCR path in AEM to Plugins configuration.
     * @param abstractTemplateLimitPath JCR path in AEM to search for abstract template pages.
     * @return Collection of mapped GatherContent and AEM fields.
     * @throws LoginException      If an error occurs during getting ResourceResolver
     * @throws RepositoryException If any error occurs during access JCR Repository
     */
    Map<String, Map<String, String>> getFieldsMappings(List<GCConfig> gcConfigs, boolean useAbstract, boolean addEmptyValue,
                                                       String templatePath, String configurationPath, String abstractTemplateLimitPath)
            throws LoginException, RepositoryException;

}
