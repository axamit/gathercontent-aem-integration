/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins;

import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.core.filters.SystemFieldFilter;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.GCUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Plugin for transformation 'text' GatherContent type into AEM tags.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component(metatype = true, immediate = true)
public final class TagsPlugin extends ExportOnlyPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagsPlugin.class);
    private static final String DEFAULT_TAGS_PATH = "/etc/tags/gathercontent";
    private static final String DEFAULT_TAGS_SEPARATOR = ",";

    @org.apache.felix.scr.annotations.Property(label = "Tags path", description = "Path for created tags",
            value = DEFAULT_TAGS_PATH)
    private static final String TAGS_PATH_PN = "tags.path";

    @org.apache.felix.scr.annotations.Property(label = "Tags separator", description = "Separator for tags in GC",
            value = DEFAULT_TAGS_SEPARATOR)
    private static final String TAGS_SEPARATOR_PN = "tags.separator";

    private String tagsPath;
    private String tagsSeparator;

    /**
     * Service activation method.
     *
     * @param properties ComponentContext.
     */
    @Activate
    @Modified
    void activate(final Map<String, Object> properties) {
        tagsPath = PropertiesUtil.toString(properties.get(TAGS_PATH_PN), DEFAULT_TAGS_PATH);
        tagsSeparator = PropertiesUtil.toString(properties.get(TAGS_SEPARATOR_PN), DEFAULT_TAGS_SEPARATOR);
    }

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
            throws RepositoryException {
        return SystemFieldFilter.INSTANCE.filter(properties);
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String propertyPath, final GCElement gcElement,
                                     final Collection<String> updatedProperties, final Map<String, List<Asset>> gcAssets)
            throws RepositoryException {
        Node node = page.adaptTo(Node.class);
        TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
        String[] tags = gcElement.getValue().trim().split("\\s*" + tagsSeparator + "\\s*");
        List<String> tagPaths = new ArrayList<>();
        for (String tagName : tags) {
            String validName = GCUtil.createValidName(tagName);
            try {
                if (!validName.isEmpty()) {
                    Tag tag = tagManager.createTag(GCStringUtil.appendNewLevelToPath(tagsPath, validName), validName, validName);
                    if (tag != null) {
                        tagPaths.add(tag.getPath());
                    }
                }
            } catch (InvalidTagFormatException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
        String propertyName = GCStringUtil.getPropertyNameFromPropertyPath(propertyPath);
        Node destinationNode = node.getNode(relativePath);
        if (!tagPaths.isEmpty()) {
            destinationNode.setProperty(propertyName, tagPaths.toArray(new String[tagPaths.size()]));
        }
    }
}
