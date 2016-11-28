<%@page session="false" %>
<%@include file="/libs/foundation/global.jsp" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %>

<%
    String MAPPING_SELECTOR = "mapping-";
    String selectors[] = slingRequest.getRequestPathInfo().getSelectors();
    String mappingPath = "mapping-list";
    String resourceType = "gathercontent/components/content/mapping-list";

    for (String selector : selectors) {
        if (selector.startsWith(MAPPING_SELECTOR)) {
            mappingPath += "/" + selector.substring(MAPPING_SELECTOR.length());
            resourceType = "gathercontent/components/content/mapping";
            break;
        }
    }
%>

<cq:include path="<%=mappingPath%>" resourceType="<%=resourceType%>"/>