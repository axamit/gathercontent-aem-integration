var GCStringUtils = GCStringUtils || {};
(function (context, $) {

    context.isEmpty = function (strToCheck) {
        return (!strToCheck || 0 === strToCheck.length);
    };
    context.isBlank = function (strToCheck) {
        return (!strToCheck.trim());
    };
    context.isEmptyOrContainsOnlyWhiteSpaces = function (strToCheck) {
        return (strToCheck.length === 0 || !strToCheck.trim());
    };
    context.getMappingListUrl = function () {
        var configName = context.getConfigurationNameFromURL();
        return window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1).concat(configName)
            .concat(".mapping.html");
    };
    context.getExportMappingListUrl = function () {
        var configName = context.getConfigurationNameFromURL();
        return window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1).concat(configName)
            .concat(".mappingexport.html");
    };
    context.getConfigurationNameFromURL = function () {
        return window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1, window.location.pathname.indexOf('.'));
    };
    context.getExportURL = function () {
        var configName = context.getConfigurationNameFromURL();
        return window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1).concat(configName)
            .concat(".export.html");
    };
        context.getAddExportMappingURL = function () {
            var configName = context.getConfigurationNameFromURL();
            return window.location.href.substring(0, window.location.href.lastIndexOf("/") + 1).concat(configName)
                .concat(".mappingexport.mapping-mapping_").concat(Date.now() / 1000 | 0).concat(".html");
        };

    /**
     * Here's the current MDN recommendation
     * that uses a regular expression in place of the deprecated unescape function for
     * encoding from UTF8 to base64:
     * @param str
     * @returns {string}
     */
    context.utf8_to_b64 = function (str) {
        return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, function (match, p1) {
            return String.fromCharCode('0x' + p1);
        }));
    };

    context.b64_to_utf8 = function (str) {
        return decodeURIComponent(Array.prototype.map.call(atob(str), function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
    };

    /**
     * fixedEncodeURI
     * Also note that if one wishes to follow the more recent RFC3986 for URLs,
     * which makes square brackets reserved (for IPv6) and thus not encoded
     * when forming something which could be part of a URL (such as a host)
     */
    context.fixedEncodeURI = function (str) {
        return encodeURI(str).replace(/%5B/g, '[').replace(/%5D/g, ']');
    };

    /**
     *fixedEncodeURIComponent
     *To be more stringent in adhering to RFC 3986 (which reserves !, ', (, ), and *),
     *even though these characters have no formalized URI delimiting uses,
     *the following can be safely used:
     */
    context.fixedEncodeURIComponent = function (str) {
        return encodeURIComponent(str).replace(/[!'()*]/g, function (c) {
            return '%' + c.charCodeAt(0).toString(16);
        });
    };

    context.getAllSpacesBeforeLetter = function (str) {
        return str.substring(0, str.indexOf(str.trim()));
    };
    context.entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
    };
    context.escapeHTML = function (string) {
        return String(string).replace(/[&<>"'\/]/g, function (s) {
            return context.entityMap[s];
        });
    };

    context.escapeJQuerySelector = function (string) {
        return string.replace(/([ #;&,.%+*~':"!^$[\]()=<>|\/@])/g, '\\$1');
    };
})(GCStringUtils, jQuery);