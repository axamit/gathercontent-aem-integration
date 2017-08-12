/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCTime</code> class represents time with timezone e.g. in GatherContent items.
 *
 * @author Axamit, gc.support@axamit.com
 * @see <a href="https://gathercontent.com/developers/items/get-items-by-id/">Item</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCTime {
    private String date;
    private String timezoneType;
    private String timezone;

    /**
     * @return Date like '2015-08-26 15:16:02.000000'.
     */
    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    /**
     * @return Timezone type like '3'.
     */
    @JsonProperty("timezone_type")
    public String getTimezoneType() {
        return timezoneType;
    }

    @JsonProperty("timezone_type")
    public void setTimezoneType(final String timezoneType) {
        this.timezoneType = timezoneType;
    }

    /**
     * @return Timezone type like 'UTC'.
     */
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }
}
