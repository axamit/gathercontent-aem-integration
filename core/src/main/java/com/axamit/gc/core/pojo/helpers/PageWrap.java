/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.pojo.helpers;

import com.axamit.gc.core.util.GCStringUtil;
import com.day.cq.wcm.api.Page;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Objects;

/**
 * Class <code>PageWrap</code> to represent easy version of CQ Page.
 *
 * @author Axamit, gc.support@axamit.com
 */
public class PageWrap implements Comparable<PageWrap> {
    private String escapedPath;
    private String path;
    private final String title;
    private final String type;
    private static final String JCR_TITLE = "jcr:title";
    private static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

    /**
     * Constructor of Easy Page Wrap version.
     *
     * @param resource resource of page.
     */
    public PageWrap(final Resource resource) {
        ValueMap properties = resource.adaptTo(ValueMap.class);
        path = resource.getPath();
        escapedPath = Text.escapeIllegalJcrChars(path);
        String typeProp = properties.get(JCR_PRIMARY_TYPE, String.class);
        type = "cq:Page".equals(typeProp) ? "page" : "folder";

        String titleProp = properties.get(JCR_TITLE, String.class);
        titleProp = titleProp == null
                ? GCStringUtil.getPropertyNameFromPropertyPath(path) : titleProp;
        Page containingPage = resource.adaptTo(Page.class);
        title = containingPage != null && containingPage.getTitle() != null ? containingPage.getTitle() : titleProp;
    }


    /**
     * Get title of page/folder.
     *
     * @return title.
     */
    public String getTitle() {
        return title;
    }


    /**
     * get resource path.
     *
     * @return path.
     */
    public String getPath() {
        return path;
    }

    /**
     * set path of page/folder.
     *
     * @param path path to set.
     */
    public void setPath(final String path) {
        this.path = path;
        escapedPath = Text.escapeIllegalJcrChars(this.path);
    }

    /**
     * get type: folder or page.
     *
     * @return folder or page.
     */
    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PageWrap pageWrap = (PageWrap) o;
        return Objects.equals(escapedPath, pageWrap.escapedPath)
            && Objects.equals(title, pageWrap.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(escapedPath, title);
    }

    @Override
    public int compareTo(final PageWrap o) {
        return escapedPath.compareTo(o.escapedPath);
    }

    @Override
    public String toString() {
        return "PageWrap{"
            + "escapedPath='" + escapedPath + '\''
            + ", path='" + path + '\''
            + ", title='" + title + '\''
            + ", type='" + type + '\''
            + '}';
    }
}
