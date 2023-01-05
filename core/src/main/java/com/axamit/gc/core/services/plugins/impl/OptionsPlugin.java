/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins.impl;

import com.axamit.gc.api.dto.GCContent;
import com.axamit.gc.api.dto.GCElementType;
import com.axamit.gc.api.dto.GCOption;
import com.axamit.gc.api.dto.GCTemplateField;
import com.axamit.gc.core.filters.OptionsFieldFilter;
import com.axamit.gc.core.filters.SystemFieldFilter;
import com.axamit.gc.core.services.plugins.GCPlugin;
import com.axamit.gc.core.util.Constants;
import com.axamit.gc.core.util.GCStringUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Plugin for transformation 'choice_checkbox' and 'choice_radio' GatherContent types.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class OptionsPlugin implements GCPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(OptionsPlugin.class);
    private static final String WHITESPACE_REPLACEMENT_FOR_EMPTY_LABEL = " ";
    private static final String ITEM = "item";
    private static final String JCR_TITLE = "jcr:title";
    private static final String TYPE = "type";
    private static final String CHECKBOX = "checkbox";
    private static final String RADIO = "radio";
    private static final int VALUE_INDEX = 0;
    private static final String VALUE_TEXT_SPLITTER = "=";

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
            throws RepositoryException {
        return OptionsFieldFilter.INSTANCE.filter(SystemFieldFilter.INSTANCE.filter(properties));
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String propertyPath, final GCContent gcContent,
                                     final GCTemplateField gcTemplateField, final Collection<String> updatedProperties,
                                     final Map<String, Asset> gcAssets)
            throws RepositoryException {
        if (updatedProperties.contains(propertyPath)) {
            LOGGER.warn("Already mapped, skipping: {}", propertyPath);
            return;
        }
        Node node = page.adaptTo(Node.class);
        if (node == null || !node.hasProperty(propertyPath)) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                    + "The AEM template has probably been modified after mapping. Please review.", propertyPath);
            return;
        }
        String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
        Node destinationNode = node.getNode(relativePath);
        List<GCOption> options = gcContent.getOptions();
        List<String> optionLabels = new ArrayList<>();
        options.iterator().forEachRemaining(gcOption -> optionLabels.add(gcOption.getLabel()));
        setMultipleStringProperty(destinationNode, optionLabels);

        final Node parent = destinationNode.getParent();
        final String type = gcTemplateField.getType().equals(GCElementType.CHOICE_CHECKBOX) ? CHECKBOX : RADIO;
        parent.setProperty(JCR_TITLE, gcTemplateField.getLabel());
        parent.setProperty(TYPE, type);

        updatedProperties.add(propertyPath);
    }

    @Override
    public void transformFromAEMtoGC(final ResourceResolver resourceResolver, final Page page,
                                     final GCContent gcContent, final String propertyPath, final String propertyValue) {
       if (resourceResolver != null && gcContent != null && propertyPath != null && propertyValue != null) {
           Resource resources = resourceResolver.getResource(propertyPath);
           if (resources != null && resources.hasChildren()) {
               List<GCOption> optionList = new ArrayList<>();
               for (Resource resource : resources.getChildren()) {
                   GCOption gcOption = new GCOption();
                   gcOption.setId(UUID.randomUUID().toString());
                   gcOption.setLabel(resource.getValueMap().get("value", String.class));
                   optionList.add(gcOption);
               }
               gcContent.setOptions(optionList);
           }
        }
    }

    private static String resolveOtherOptionPropertyValue(Resource resource) {
        if (resource != null) {
            String otherOptionValue = (String) resource.getValueMap().get(Constants.OTHER_OPTION_PROPERTY_NAME);
            if (otherOptionValue != null) {
                return otherOptionValue;
            }
        }
        return "";
    }

    private static List<String> resolvePropertyValues(Resource valuesResource) throws RepositoryException {
        if (valuesResource != null) {
            Property valuesProperty = valuesResource.adaptTo(Property.class);
            if (valuesProperty != null) {
                List<String> values = valuesProperty.isMultiple()
                        ? Arrays.asList(PropertiesUtil.toStringArray(valuesProperty.getValues(), new String[0]))
                        : ImmutableList.of(valuesProperty.getString());
                for (ListIterator<String> it = values.listIterator(); it.hasNext(); ) {
                    String value = it.next();
                    if (StringUtils.EMPTY.equals(value)) {
                        it.set(WHITESPACE_REPLACEMENT_FOR_EMPTY_LABEL);
                    }
                }
                return values;

            }
        }
        return ImmutableList.of();
    }

    private static void setMultipleStringProperty(final Node destinationNode, final List<String> stringValues) throws RepositoryException {
        if (!stringValues.isEmpty() && !destinationNode.hasNodes()) {
            for (int i = 0; i < stringValues.size(); i++) {
                if (!destinationNode.hasNode(ITEM + i)) {
                    final Node newNode = destinationNode.addNode(ITEM + i);
                    newNode.setProperty(Constants.JSON_PN_TEXT, stringValues.get(i));
                    newNode.setProperty(Constants.JSON_PN_VALUE, stringValues.get(i));
                }
            }
        }
    }
}
