/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCItem</code> class represents information about user.
 *
 * @see <a href="https://gathercontent.com/developers/me/get-me/">User info</a>
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GCMe {

    private String email;
    private String firstName;
    private String lastName;
    private String timezone;
    private String language;
    private String gender;
    private String avatar;

    /**
     * @return User email.
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @return User first name.
     */
    @JsonProperty("first_name")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("first_name")
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return User last name.
     */
    @JsonProperty("last_name")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("last_name")
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return User timezone like 'UTC'.
     */
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    /**
     * @return User language.
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    /**
     * @return User gender.
     */
    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    /**
     * @return URL to user avatar like 'http://image-url.com'.
     */
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(final String avatar) {
        this.avatar = avatar;
    }
}
