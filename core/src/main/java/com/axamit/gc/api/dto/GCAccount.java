/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The <code>GCAccount</code> is a POJO class represent user account info.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCAccount {
    private String id;
    private String name;
    private String slug;
    private String timezone;

    /**
     * @return ID of GatherContent account.
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return Name of GatherContent account.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return Subdomain of account on GatherContent.
     */
    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    /**
     * @return Timezone of GatherContent item.
     */
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }
}
