<%@page contentType="text/html"
        pageEncoding="utf-8"
        import="javax.jcr.Node,
                java.util.Iterator,
                com.day.cq.wcm.webservicesupport.Configuration,
                com.day.cq.wcm.webservicesupport.Service,
                org.apache.commons.lang.StringEscapeUtils,
                org.apache.sling.api.resource.Resource"
%>

<%@include file="/libs/foundation/global.jsp" %>
<%@include file="/libs/cq/cloudserviceconfigs/components/configpage/init.jsp" %>

<%
    String path = resource.getPath();
    String resourceType = resource.getResourceType();
    String dialogPath = resource.getResourceResolver().getResource(resourceType).getPath() + "/dialog";
%>
<div>
    <h1>GatherContent Credentials</h1>
    <div>
        <script type="text/javascript">
            CQ.WCM.edit({
                "path": "<%= path %>",
                "dialog": "<%= dialogPath %>",
                "type": "<%= resourceType %>",
                "editConfig": {
                    "xtype": "editbar",
                    "listeners": {
                        "afteredit": "REFRESH_PAGE"
                    },
                    "inlineEditing": CQ.wcm.EditBase.INLINE_MODE_NEVER,
                    "disableTargeting": true,
                    "actions": [
                        CQ.I18n.getMessage("Configuration"),
                        {
                            "xtype": "tbseparator"
                        },
                        CQ.wcm.EditBase.EDIT
                        <%
                        if (serviceUrl != null) {
                        %>
                        ,
                        {
                            "xtype": "tbseparator"
                        },
                        {
                            "xtype": "tbtext",
                            "text": "<a href='<%=serviceUrl%>' target='_blank' style='color: #15428B; cursor: pointer; text-decoration: underline'>" + CQ.I18n.getMessage("<%=serviceUrlLabel%>") + "</span>"
                        }
                        <%
                        }
                        %>
                    ]
                }
            });
        </script>
    </div>
    <ul>
        <li><div class="li-bullet"><strong>GatherContent User Email Address </strong><br><%= xssAPI.encodeForHTML(properties.get("gcUsername", "")) %></div></li>
        <li><div class="li-bullet"><strong>GatherContent API key </strong><br><%= xssAPI.encodeForHTML(properties.get("gcApikey", "")) %></div></li>
        <li><cq:include path="test" resourceType="gathercontent/components/content/gctest" /></li>
    </ul>
</div>