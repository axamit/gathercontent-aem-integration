/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services.impl;

import com.adobe.granite.license.ProductInfoService;
import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCStatus;
import com.axamit.gc.api.services.GCContentApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.felix.scr.annotations.*;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * OSGI service implements <tt>GCContentApi</tt> interface provides methods to get information from remote
 * GatherContent server.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCContentApi.class)
@Component(description = "GContent Api", name = "GContent Api", immediate = true, metatype = true)
public final class GCContentApiImpl implements GCContentApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCContentApiImpl.class);
    private static final String SUCCESS_POST_CALL_FLAG = "success";
    private static final String PARAM_ACCOUNT_ID = "account_id";
    private static final String PARAM_STATUS_ID = "status_id";
    private static final String JSON_DATA_NODE_NAME = "data";

    private static final String STATUS_BY_PROJECT_ID = "/projects/%s/statuses";
    private static final String UPDATE_ITEM_STATUS = "/items/%s/choose_status";

    private static String userAgentInfo;

    @Reference
    private ProductInfoService productInfoService;

    /**
     * Activates or updates the service.
     *
     * @param componentContext context.
     */
    @Activate
    @Modified
    void activate(final ComponentContext componentContext) {
        setUserAgentInfo(componentContext);
    }

    private void setUserAgentInfo(final ComponentContext componentContext) {
        String productInfoString = GCUtil.getUserAgentInfo(productInfoService);
        userAgentInfo = "Integration-AEM" + productInfoString + "/"
                + componentContext.getUsingBundle().getVersion().toString();
    }
    /**
     * @inheritDoc
     */
    @Override
    public JSONObject me(final GCContext gcContext) throws GCException {
        String content = GCUtil.apiCall("/me", gcContext, userAgentInfo);
        if ("Invalid credentials.".equals(content)) {
            LOGGER.warn(content);
            return null;
        }
        return JSONUtil.fromJsonToJSonObject(content);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCAccount> accounts(final GCContext gcContext) throws GCException {
        String content = GCUtil.apiCall("/accounts", gcContext, userAgentInfo);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCAccount.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCProject> projects(final GCContext gcContext, final Integer accountId) throws GCException {
        Collection<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_ACCOUNT_ID, String.valueOf(accountId)));
        String content = GCUtil.apiCall("/projects", gcContext, params, userAgentInfo);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCProject.class);
    }

    /**
     * @inheritDoc
     * Used to retrieve all project statuses
     */
    @Override
    public List<GCStatus> statusesByProjectId(final GCContext gcContext, final Integer projectId) throws GCException {
        String content = GCUtil.apiCall(String.format(STATUS_BY_PROJECT_ID, projectId), gcContext, userAgentInfo);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCStatus.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean updateItemStatus(final GCContext gcContext, final Integer itemId, final Integer statusId) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_STATUS_ID, String.valueOf(statusId)));
        return Boolean.parseBoolean(apiPostCall(String.format(UPDATE_ITEM_STATUS, itemId), gcContext, params, null)
                .get(SUCCESS_POST_CALL_FLAG));
    }

    private static Map<String, String> apiPostCall(final String url, final GCContext gcContext,
                                                   final List<NameValuePair> params, final String[] headers) {
        HttpPost httpPost = new HttpPost(gcContext.getApiURL() + url);
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        Map<String, String> returnObject = new HashMap<>();

        HttpClient httpClient = GCUtil.setHeadersAndAuth(httpPost, gcContext, userAgentInfo, false);
        try {
            LOGGER.debug("Requested GatherContent URL " + httpPost.getURI()
                    + System.lineSeparator() + "Request method: " + httpPost.getMethod()
                    + System.lineSeparator() + "Request entity: " + EntityUtils.toString(httpPost.getEntity(), StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_ACCEPTED) {
                String responseEntityContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                LOGGER.error("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse
                        + System.lineSeparator() + "ResponseEntity: " + responseEntityContent);
            } else if (headers != null) {
                LOGGER.debug("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse
                        + System.lineSeparator() + "ResponseEntity: " + EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
                for (String header : headers) {
                    Header firstHeader = httpResponse.getFirstHeader(header);
                    if (firstHeader != null) {
                        returnObject.put(header, httpResponse.getFirstHeader(header).getValue());
                    }
                }
            }
            returnObject.put(SUCCESS_POST_CALL_FLAG, Boolean.toString(statusCode == HttpStatus.SC_ACCEPTED));
        } catch (IOException e) {
            LOGGER.error("Request to GatherContent URL: {} failed. {}", httpPost.getURI(), e.getMessage());
        }
        return returnObject;
    }
}
