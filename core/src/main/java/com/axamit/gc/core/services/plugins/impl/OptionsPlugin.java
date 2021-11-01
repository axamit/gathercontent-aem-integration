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
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
                                     final GCContent gcContent, final String propertyPath, final String propertyValue)
            throws RepositoryException {
        //TODO
//        if (resourceResolver != null && gcContent != null && propertyPath != null && propertyValue != null) {
//            Resource valuesResource = resourceResolver.getResource(
//                    GCStringUtil.appendNewLevelToPath(propertyPath, propertyValue));
//            List<String> values = resolvePropertyValues(valuesResource);
//
//            Resource defaultValuesResource =
//                    resourceResolver.getResource(GCStringUtil.appendNewLevelToPath(propertyPath,
//                            Constants.DEFAULT_SELECTION_PN));
//            List<String> defaultValues = resolvePropertyValues(defaultValuesResource);
//
//            List<GCOption> optionList = new ArrayList<>();

//            if (gcContent.getType().equals(GCElementType.MULTIVALUE_NEW_EDITOR)) {
//                optionList = gcElement.getOptions();
//                Iterator<GCOption> optionIterator = optionList.iterator();
//                Iterator<String> valueIterator = values.iterator();
//                while (optionIterator.hasNext() && valueIterator.hasNext()) {
//                    final String checkboxValue = valueIterator.next().split(VALUE_TEXT_SPLITTER)[VALUE_INDEX];
//                    final boolean selected = defaultValues.contains(checkboxValue);
//                    optionIterator.next().setSelected(selected);
//                }
//            } else {
//                for (String value : values) {
//                    GCOption gcOption = new GCOption();
//                    gcOption.setName("op" + UUID.randomUUID());
//                    gcOption.setLabel(value);
//                    if (defaultValues.contains(value)) {
//                        gcOption.setSelected(true);
//                    } else {
//                        gcOption.setSelected(false);
//                    }
//                    gcOption.setValue(null);
//                    optionList.add(gcOption);
//                }
//            }
//            if (!optionList.isEmpty()) {
//                if (gcElement.getOtherOption() != null && gcElement.getOtherOption()) {
//                    optionList.get(optionList.size() - 1)
//                            .setEscapedValue(resolveOtherOptionPropertyValue(resourceResolver.getResource(propertyPath)));
//                }
//                gcElement.setOptions(optionList);
//            }
//        }
    }

//    private static String resolveOtherOptionPropertyValue(Resource resource) {
//        if (resource != null) {
//            String otherOptionValue = (String) resource.getValueMap().get(Constants.OTHER_OPTION_PROPERTY_NAME);
//            if (otherOptionValue != null) {
//                return otherOptionValue;
//            }
//        }
//        return "";
//    }
//
//    private static List<String> resolvePropertyValues(Resource valuesResource) throws RepositoryException {
//        if (valuesResource != null) {
//            Property valuesProperty = valuesResource.adaptTo(Property.class);
//            if (valuesProperty != null) {
//                List<String> values = valuesProperty.isMultiple()
//                        ? Arrays.asList(PropertiesUtil.toStringArray(valuesProperty.getValues(), new String[0]))
//                        : ImmutableList.of(valuesProperty.getString());
//                for (ListIterator<String> it = values.listIterator(); it.hasNext(); ) {
//                    String value = it.next();
//                    if (StringUtils.EMPTY.equals(value)) {
//                        it.set(WHITESPACE_REPLACEMENT_FOR_EMPTY_LABEL);
//                    }
//                }
//                return values;
//
//            }
//        }
//        return ImmutableList.of();
//    }

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
