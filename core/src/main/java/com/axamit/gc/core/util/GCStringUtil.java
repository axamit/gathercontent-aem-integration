/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * The <code>GCUtil</code> is an utility class presenting functionality manipulations with strings.
 *
 * @author Axamit, gc.support@axamit.com
 */
public enum GCStringUtil {
    /*INSTANCE*/;
    private static final String SLASH = "/";

    /**
     * Get relative path to JCR Node containing property from relative path to property.
     * 'jcr:content/par/radio/options' will return 'jcr:content/par/radio',
     * 'jcr:content/jcr:title' will return 'jcr:content'.
     *
     * @param propertyPath Relative path to property.
     * @return Path to Node.
     */
    public static String getRelativeNodePathFromPropertyPath(final String propertyPath) {
        return propertyPath.substring(0, propertyPath.lastIndexOf(SLASH));
    }

    /**
     * Get property name from path to property. 'jcr:content/par/radio/options' will return 'options',
     * 'jcr:content/jcr:title' will return 'jcr:title'.
     *
     * @param propertyPath Relative path to property.
     * @return Property name.
     */
    public static String getPropertyNameFromPropertyPath(final String propertyPath) {
        return propertyPath.substring(propertyPath.lastIndexOf(SLASH) + 1, propertyPath.length());
    }

    /**
     * If string starts with slash strip it.
     *
     * @param value String
     * @return Stripped string.
     */
    public static String stripFirstSlash(final String value) {
        return StringUtils.stripStart(value, SLASH);
    }

    /**
     * Obtain path to parent node.
     *
     * @param path Path to node/property.
     * @return Parent path.
     */
    public static String getParentPath(final String path) {
        return path.substring(0, path.lastIndexOf(SLASH));
    }

    /**
     * Obtain last part of URL.
     *
     * @param location Path.
     * @return Return last part of URL if it is not null, null otherwise.
     */
    public static String getLastURLPartOrNull(String location) {
        if (StringUtils.isBlank(location)) {
            return null;
        }
        String[] split = location.split(SLASH);
        if (split.length == 0) {
            return null;
        }
        return split[split.length - 1];
    }

    /**
     * Add new part to path.
     *
     * @param basePath Path.
     * @param appendix New part to add.
     * @return Concatenated path.
     */
    public static String appendNewLevelToPath(String basePath, String appendix) {
        return StringUtils.defaultString(basePath) + SLASH + StringUtils.defaultString(appendix);
    }

}
