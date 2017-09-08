/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;

import com.adobe.granite.license.ProductInfo;
import com.adobe.granite.license.ProductInfoService;
import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCData;
import com.axamit.gc.api.dto.GCFile;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    private static final String PARAM_PARENT_ID = "parent_id";
    private static final String PARAM_PROJECT_ID = "project_id";
    private static final String PARAM_STATUS_ID = "status_id";
    private static final String PARAM_TEMPLATE_ID = "template_id";
    private static final String PARAM_CONFIG = "config";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_TYPE = "type";
    private static final String JSON_DATA_NODE_NAME = "data";
    private static final String AEM_PRODUCT_INFO_NAME = "Adobe Experience Manager";
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
        ProductInfo[] infos = productInfoService.getInfos();
        String productInfoString = StringUtils.EMPTY;
        if (infos != null && infos.length > 0) {
            ProductInfo aemProductInfo = null;
            for (ProductInfo productInfo : infos) {
                if (AEM_PRODUCT_INFO_NAME.equals(productInfo.getName())) {
                    aemProductInfo = productInfo;
                    break;
                }
            }
            if (aemProductInfo == null) {
                aemProductInfo = infos[0];
            }
            productInfoString = "-" + aemProductInfo.getVersion().toString();
        }
        userAgentInfo = "Integration-AEM" + productInfoString + "/"
                + componentContext.getUsingBundle().getVersion().toString();
    }

    /**
     * @inheritDoc
     */
    @Override
    public JSONObject me(final GCContext gcContext) throws GCException {
        String content = apiCall("/me", gcContext);
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
        String content = apiCall("/accounts", gcContext);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCAccount.class);
    }

    private static String apiCall(final String url, final GCContext gcContext) throws GCException {
        return apiCall(url, gcContext, null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public GCProject project(final GCContext gcContext, final String projectID) throws GCException {
        String content = apiCall("/projects/" + projectID, gcContext);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToObject(extractedResult, GCProject.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean createProject(final GCContext gcContext, final String accountId, final String projectName,
                                 final String type) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_ACCOUNT_ID, accountId));
        params.add(new BasicNameValuePair(PARAM_NAME, projectName));
        params.add(new BasicNameValuePair(PARAM_TYPE, type));
        return Boolean.parseBoolean(apiPostCall("/projects", gcContext, params, null).get(SUCCESS_POST_CALL_FLAG));
    }

    private static Map<String, String> apiPostCall(final String url, final GCContext gcContext,
                                                   final List<NameValuePair> params, final String[] headers) {
        HttpPost httpPost = new HttpPost(gcContext.getApiURL() + url);
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        Map<String, String> returnObject = new HashMap<>();

        HttpClient httpClient = setHeadersAndAuth(httpPost, gcContext);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_ACCEPTED) {
                String responseEntityContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                LOGGER.error("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse.toString()
                        + System.lineSeparator() + "ResponseEntity: " + responseEntityContent);
            } else if (headers != null) {
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

    /**
     * @inheritDoc
     */
    @Override
    public GCTemplate template(final GCContext gcContext, final String projectId,
                               final String templateId) throws GCException {
        List<GCTemplate> templates = templates(gcContext, projectId);
        for (GCTemplate template : templates) {
            if (templateId.equals(template.getId())) {
                return template;
            }
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public GCItem itemById(final GCContext gcContext, final String itemId) throws GCException {
        String content = apiCall("/items/" + itemId, gcContext);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToObject(extractedResult, GCItem.class);
    }

    private static String apiCall(final String url, final GCContext gcContext, final Iterable<NameValuePair> params)
            throws GCException {
        StringBuilder requestUrl = new StringBuilder(gcContext.getApiURL()).append(url);
        if (params != null) {
            requestUrl.append("?").append(URLEncodedUtils.format(params, StandardCharsets.UTF_8));
        }
        HttpUriRequest httpUriRequest = new HttpGet(requestUrl.toString());

        HttpClient httpClient = setHeadersAndAuth(httpUriRequest, gcContext);

        try {
            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8.name());
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
            }
            if (statusCode == HttpStatus.SC_OK) {
                return stringBuilder.toString(); //new String(httpResponse.getEntity().getContent());
            } else {
                throw new GCException("Requested GatherContent URL: " + httpUriRequest.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse.toString()
                        + System.lineSeparator() + "ResponseEntity: " + stringBuilder.toString());
            }
        } catch (IOException e) {
            LOGGER.error("Request to GatherContent URL: {} failed. {}", httpUriRequest.getURI(), e.getMessage());
            throw new GCException(e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCItem> itemsByProjectIdAndTemplateId(final GCContext gcContext, final String projectId,
                                                      final String templateId, final boolean fetch) throws GCException {
        List<GCItem> allItems = itemsByProjectId(gcContext, projectId);
        List<GCItem> filteredItems = new ArrayList<>();
        for (GCItem gCItem : allItems) {
            if (templateId.equals(gCItem.getTemplateId())) {
                filteredItems.add(gCItem);
            }
        }
        if (fetch) {
            for (int i = 0; i < filteredItems.size(); i++) {
                filteredItems.set(i, itemById(gcContext, filteredItems.get(i).getId()));
            }
        }
        return filteredItems;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCData> statusesByProjectId(final GCContext gcContext, final String projectId) throws GCException {
        String content = apiCall("/projects/" + projectId + "/statuses", gcContext);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCData.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCFile> filesByItemId(final GCContext gcContext, final String itemId) throws GCException {
        String content = apiCall("/items/" + itemId + "/files", gcContext);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCFile.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean updateItemStatus(final GCContext gcContext, final String itemId, final String statusId) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_STATUS_ID, statusId));
        return Boolean.parseBoolean(apiPostCall("/items/" + itemId + "/choose_status", gcContext, params, null)
                .get(SUCCESS_POST_CALL_FLAG));
    }

    private static HttpClient setHeadersAndAuth(final HttpRequest httpUriRequest, final GCContext gcContext) {
        for (Map.Entry<String, String> entry : gcContext.getHeaders().entrySet()) {
            Header header = new BasicHeader(entry.getKey(), entry.getValue());
            httpUriRequest.setHeader(header);
        }
        httpUriRequest.addHeader(new BasicHeader(HttpHeaders.USER_AGENT, userAgentInfo));
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        String username = gcContext.getUsername();
        String apiKey = gcContext.getApikey();
        if (username != null && apiKey != null) {
            //this is hack for httpclient 4.3.4
            try {
                Credentials credentials = new UsernamePasswordCredentials(username, apiKey);
                httpUriRequest.addHeader(new BasicScheme().authenticate(credentials, httpUriRequest, null));
            } catch (AuthenticationException e) {
                LOGGER.error(e.getMessage(), e);
            }

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, apiKey));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        return httpClientBuilder.build();
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCProject> projects(final GCContext gcContext, final String accountId) throws GCException {
        Collection<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_ACCOUNT_ID, accountId));
        String content = apiCall("/projects", gcContext, params);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCProject.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCTemplate> templates(final GCContext gcContext, final String projectID) throws GCException {
        Collection<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_PROJECT_ID, projectID));
        String content = apiCall("/templates", gcContext, params);

        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCTemplate.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCItem> itemsByProjectId(final GCContext gcContext, final String projectId) throws GCException {
        Collection<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_PROJECT_ID, projectId));
        String content = apiCall("/items", gcContext, params);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCItem.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean applyItemTemplate(final GCContext gcContext, final String itemId, final String templateId) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_TEMPLATE_ID, templateId));
        return Boolean.parseBoolean(apiPostCall("/items/" + itemId + "/apply_template", gcContext, params, null)
                .get(SUCCESS_POST_CALL_FLAG));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String createItem(final GCItem gcItem, final GCContext gcContext) throws GCException {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_PROJECT_ID, gcItem.getProjectId()));
        params.add(new BasicNameValuePair(PARAM_NAME, gcItem.getName()));
        params.add(new BasicNameValuePair(PARAM_PARENT_ID, gcItem.getParentId()));
        params.add(new BasicNameValuePair(PARAM_TEMPLATE_ID, gcItem.getTemplateId()));

        List<GCConfig> gcConfigs = gcItem.getConfig();
        if (gcConfigs != null && gcConfigs.size() != NumberUtils.INTEGER_ZERO) {
            String itemJSON = JSONUtil.fromObjectToJsonString(gcConfigs);
            byte[] encodedBytes = Base64.encodeBase64(itemJSON.getBytes(StandardCharsets.UTF_8));
            params.add(new BasicNameValuePair(PARAM_CONFIG, new String(encodedBytes, StandardCharsets.UTF_8)));
        }
        Map<String, String> returnObject = apiPostCall("/items/", gcContext, params, new String[]{HttpHeaders.LOCATION});
        String id = GCStringUtil.getLastURLPartOrNull(returnObject.get(HttpHeaders.LOCATION));
        if (Boolean.parseBoolean(returnObject.get(SUCCESS_POST_CALL_FLAG))) {
            return id;
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean updateItem(final List<GCConfig> gcConfigs, final String itemId, final GCContext gcContext)
            throws GCException {
        List<NameValuePair> params = new ArrayList<>();

        String itemJSON = JSONUtil.fromObjectToJsonString(gcConfigs);
        byte[] encodedBytes = Base64.encodeBase64(itemJSON.getBytes(StandardCharsets.UTF_8));

        params.add(new BasicNameValuePair(PARAM_CONFIG, new String(encodedBytes, StandardCharsets.UTF_8)));

        return Boolean.parseBoolean(apiPostCall("/items/" + itemId + "/save", gcContext, params, null)
                .get(SUCCESS_POST_CALL_FLAG));
    }
}
