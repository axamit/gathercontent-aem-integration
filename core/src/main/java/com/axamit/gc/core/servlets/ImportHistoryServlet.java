/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.core.servlets;

import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.sightly.models.ImportModel;
import com.axamit.gc.core.util.JSONUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Servlet return list of imports in JSON format e.g.
 * '/etc/cloudservices/gathercontent/gathercontent-importer.importhistory.json'
 *
 * @author Axamit, gc.support@axamit.com
 */
@SlingServlet(
        resourceTypes = {"sling/servlet/default"},
        selectors = {"importhistory"},
        methods = {HttpConstants.METHOD_POST}
)
public final class ImportHistoryServlet extends GCAbstractServlet {

    private static final String DEFAULT_ORDER_BY_FIELD = "importId";
    private static final String DEFAULT_ORDER_DIRECTION = "desc";

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        final String data = request.getRequestParameter("data").getString();
        try {
            final JSONObject jsonObject = JSONUtil.fromJsonToJSonObject(data);
            final String draw = jsonObject.getString("draw");
            final String length = jsonObject.getString("length");
            final String start = jsonObject.getString("start");
            String orderBy = DEFAULT_ORDER_BY_FIELD;
            String orderDirection = DEFAULT_ORDER_DIRECTION;
            final JSONArray orderArray = jsonObject.getJSONArray("order");
            final JSONArray columnsArray = jsonObject.getJSONArray("columns");
            if (orderArray != null && orderArray.length() > 0 && columnsArray != null && columnsArray.length() > 0) {
                JSONObject order = orderArray.getJSONObject(0);
                orderBy = columnsArray.getJSONObject(order.getInt("column")).getString("data");
                orderDirection = order.getString("dir");
            }
            final List<ImportModel> importList = new ArrayList<>();
            final ResourceResolver resourceResolver = request.getResourceResolver();
            final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            final Page containingPage = pageManager.getContainingPage(request.getResource());
            final Map<String, String> queryParams = new HashMap<>();
            queryParams.put("path", containingPage.getPath());
            queryParams.put("type", "nt:unstructured");
            queryParams.put("1_property", "sling:resourceType");
            queryParams.put("1_property.value", "gathercontent/components/content/import");
            queryParams.put("2_property", "status");
            queryParams.put("2_property.operation", "exists");
            queryParams.put("orderby", "@" + orderBy);
            queryParams.put("orderby.sort", orderDirection);
            final PredicateGroup predicateGroup = PredicateGroup.create(queryParams);

            final Query query = queryBuilder.createQuery(predicateGroup,
                    request.getResourceResolver().adaptTo(Session.class));
            query.setStart(Long.parseLong(start));
            query.setHitsPerPage(Long.parseLong(length));
            final SearchResult result = query.getResult();
            final Iterator<Resource> resources = result.getResources();
            while (resources.hasNext()) {
                final Resource next = resources.next();
                final ImportModel importModel = next.adaptTo(ImportModel.class);
                if (importModel != null) {
                    importList.add(importModel);
                }
            }

            final JSONObject responseJSON = new JSONObject();
            responseJSON.put("draw", draw);
            responseJSON.put("recordsTotal", result.getTotalMatches());
            responseJSON.put("recordsFiltered", result.getTotalMatches());
            final JSONArray importListJSON = new JSONArray();
            for (ImportModel importModel : importList) {
                final JSONObject importModelJSON = new JSONObject();
                importModelJSON.put(ImportModel.PROPERTY_IMPORT_ID,
                        importModel.getImportId() != null ? importModel.getImportId() : "");
                importModelJSON.put(ImportModel.PROPERTY_JOB_TYPE,
                        importModel.getJobType() != null ? importModel.getJobType() : "");
                importModelJSON.put(ImportModel.PROPERTY_PROJECT_NAME,
                        importModel.getProjectName() != null ? importModel.getProjectName() : "");
                importModelJSON.put(ImportModel.PROPERTY_IMPORT_START_DATE,
                        importModel.getFormattedStartDate() != null ? importModel.getFormattedStartDate() : "");
                importModelJSON.put(ImportModel.PROPERTY_IMPORT_END_DATE,
                        importModel.getFormattedEndDate() != null ? importModel.getFormattedEndDate() : "");
                importModelJSON.put(ImportModel.PROPERTY_STATUS, importModel.getStatus());
                importModelJSON.put("historyPath", importModel.getHistoryPath());
                importListJSON.put(importModelJSON);
            }
            responseJSON.put("data", importListJSON);
            response.getWriter().write(responseJSON.toString());
        } catch (GCException | JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
