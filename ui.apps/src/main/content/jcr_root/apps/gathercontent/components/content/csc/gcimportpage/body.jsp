<%@page session="false" %><%--
  Copyright 1997-2009 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================



--%>
<%@page contentType="text/html"
        pageEncoding="utf-8"
        import="javax.jcr.Node,
                java.util.Iterator,
                com.day.cq.wcm.webservicesupport.Configuration,
                com.day.cq.wcm.webservicesupport.Service,
                org.apache.commons.lang.StringEscapeUtils,
                org.apache.sling.api.resource.Resource" %>
<%@include file="/libs/foundation/global.jsp" %>
<%
    String selectors[] = slingRequest.getRequestPathInfo().getSelectors();
    String currentPagePath = currentPage.getPath();
    String content = "content.jsp";
    if (selectors != null && selectors.length > 0) {
        content = "content." + selectors[0] + ".jsp";
    }
%>
<body>
    <ul>
        <li><a href="<%=currentPagePath%>.html">Import</a></li>
        <li><a href="<%=currentPagePath%>.update.html">Update</a></li>
        <li><a href="<%=currentPagePath%>.mapping.html">Mappings</a></li>
        <li><a href="<%=currentPagePath%>.credentials.html">GC Credentials</a></li>
    </ul>
    <p class="cq-clear-for-ie7"></p>
    <cq:include script="<%=content%>"/>
</body>
