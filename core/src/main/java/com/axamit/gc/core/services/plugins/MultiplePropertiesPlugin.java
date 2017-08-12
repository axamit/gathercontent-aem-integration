/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.services.plugins;

import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.api.dto.GCOption;
import com.axamit.gc.core.filters.MultipleFieldFilter;
import com.axamit.gc.core.filters.SystemFieldFilter;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Plugin for transformation 'choice_checkbox' and 'choice_radio' GatherContent types.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCPlugin.class)
@Component
public final class MultiplePropertiesPlugin implements GCPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiplePropertiesPlugin.class);
    private static final String WHITESPACE_REPLACEMENT_FOR_EMPTY_LABEL = " ";

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

    private static void setMultipleStringProperty(final Node destinationNode, final String propertyName,
                                                  final List<String> stringValues) throws RepositoryException {
        if (!stringValues.isEmpty()) {
            if (destinationNode.hasProperty(propertyName)) {
                Property property = destinationNode.getProperty(propertyName);
                if (property != null) {
                    property.remove();
                }
            }
            if (stringValues.size() > 1) {
                destinationNode.setProperty(propertyName, stringValues.toArray(new String[stringValues.size()]));
            } else {
                destinationNode.setProperty(propertyName, stringValues.get(0));
            }
        }
    }

    @Override
    public Collection<Property> filter(final ResourceResolver resourceResolver, final Collection<Property> properties)
        throws RepositoryException {
        return MultipleFieldFilter.INSTANCE.filter(SystemFieldFilter.INSTANCE.filter(properties));
    }

    @Override
    public void transformFromGCtoAEM(final ResourceResolver resourceResolver, final Page page,
                                     final String propertyPath, final GCElement gcElement,
                                     final Collection<String> updatedProperties,
                                     final Map<String, List<Asset>> gcAssets)
        throws RepositoryException {
        Node node = page.adaptTo(Node.class);
        if (node == null || !node.hasProperty(propertyPath)) {
            LOGGER.warn("Property '{}' does not exist in the AEM template. "
                + "The AEM template has probably been modified after mapping. Please review.", propertyPath);
            return;
        }
        String relativePath = GCStringUtil.getRelativeNodePathFromPropertyPath(propertyPath);
        String propertyName = GCStringUtil.getPropertyNameFromPropertyPath(propertyPath);
        Node destinationNode = node.getNode(relativePath);
        List<GCOption> options = gcElement.getOptions();
        List<String> optionLabels = new ArrayList<>();
        List<String> selectedLabels = new ArrayList<>();
        for (GCOption option : options) {
            String optionValueOrLabel = option.getLabel();
            String optionValue = option.getValue();
            if (StringUtils.isNotEmpty(optionValue)) {
                optionValueOrLabel = optionValue;
            }
            optionLabels.add(optionValueOrLabel);
            if (option.getSelected()) {
                selectedLabels.add(optionValueOrLabel);
            }
        }
        for (final ListIterator<String> i = optionLabels.listIterator(); i.hasNext(); ) {
            final String optionLabel = i.next();
            i.set(optionLabel.replace("=", "\\="));
        }

        setMultipleStringProperty(destinationNode, Constants.OTHER_OPTION_PROPERTY_NAME, ImmutableList.of(""));
        setMultipleStringProperty(destinationNode, propertyName, optionLabels);
        setMultipleStringProperty(destinationNode, Constants.DEFAULT_SELECTION_PN, selectedLabels);
    }

    @Override
    public void transformFromAEMtoGC(final ResourceResolver resourceResolver, final Page page,
                                     final GCElement gcElement, final String propertyPath, final String propertyValue)
        throws RepositoryException {
        if (resourceResolver != null && gcElement != null && propertyPath != null && propertyValue != null) {
            Resource valuesResource = resourceResolver.getResource(
                GCStringUtil.appendNewLevelToPath(propertyPath, propertyValue));
            List<String> values = resolvePropertyValues(valuesResource);

            Resource defaultValuesResource =
                resourceResolver.getResource(GCStringUtil.appendNewLevelToPath(propertyPath,
                    Constants.DEFAULT_SELECTION_PN));
            List<String> defaultValues = resolvePropertyValues(defaultValuesResource);

            List<GCOption> optionList = new ArrayList<>();
            for (String value : values) {
                GCOption gcOption = new GCOption();
                gcOption.setName("op" + UUID.randomUUID());
                gcOption.setLabel(value);
                if (defaultValues.contains(value)) {
                    gcOption.setSelected(true);
                } else {
                    gcOption.setSelected(false);
                }
                gcOption.setValue(null);
                optionList.add(gcOption);
            }
            if (!optionList.isEmpty()) {
                if (gcElement.getOtherOption() != null && gcElement.getOtherOption()) {
                    optionList.get(optionList.size() - 1)
                        .setEscapedValue(resolveOtherOptionPropertyValue(resourceResolver.getResource(propertyPath)));
                }
                gcElement.setOptions(optionList);
            }
        }
    }
}
