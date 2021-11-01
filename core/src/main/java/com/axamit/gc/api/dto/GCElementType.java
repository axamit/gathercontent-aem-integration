///*
// * Axamit, gc.support@axamit.com
// */
//
//package com.axamit.gc.api.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
///**
// * The <code>GCElementType</code> class represents element type of config.
// *
// * @author Axamit, gc.support@axamit.com
// * @see <a href="https://gathercontent.com/developers/the-config-field/">Element types</a>
// */
//@JsonIgnoreProperties(ignoreUnknown = true)
//public enum GCElementType {
//    @JsonProperty("text")
//    TEXT("text"),
//    @JsonProperty("files")
//    FILES("files"),
//    @JsonProperty("choice_radio")
//    CHOICE_RADIO("choice_radio"),
//    @JsonProperty("choice_checkbox")
//    CHOICE_CHECKBOX("choice_checkbox"),
//    @JsonProperty("section")
//    SECTION("section"),
//    MULTIVALUE_NEW_EDITOR("multivalue_new_editor");
//    private final String type;
//
//    /**
//     * Constructor.
//     *
//     * @param type String representation of type.
//     */
//    GCElementType(final String type) {
//        this.type = type;
//    }
//
//    public String getValue() {
//        return type;
//    }
//}
/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The <code>GCElementType</code> class represents element type of config.
 *
 * @author Axamit, gc.support@axamit.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum GCElementType {

    @JsonProperty("text")
    TEXT("text"),

    @JsonProperty("attachment")
    FILES("files"),

    @JsonProperty("choice_radio")
    CHOICE_RADIO("choice_radio"),

    @JsonProperty("choice_checkbox")
    CHOICE_CHECKBOX("choice_checkbox"),

    //type to merge 'choice_checkbox' & 'choice_radio' into one type as they have one structure
    OPTIONS("options"),

    @JsonProperty("guidelines")
    GUIDELINES("guidelines"),

    @JsonProperty("component")
    COMPONENT("component"),

    NOT_SUPPORTED_TYPE("not_supported_type");

    private final String value;

    /**
     * Constructor.
     *
     * @param type String representation of type.
     */
    GCElementType(final String type) {
        this.value = type;
    }

    public String getValue() {
        return value;
    }
}
