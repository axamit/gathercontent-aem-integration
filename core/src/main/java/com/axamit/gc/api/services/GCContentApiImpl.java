/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;

import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCAccount;
import com.axamit.gc.api.dto.GCData;
import com.axamit.gc.api.dto.GCFile;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCProject;
import com.axamit.gc.api.dto.GCTemplate;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
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
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * OSGI service implements <tt>GCContentApi</tt> interface provides methods to get information from remote
 * GatherContent server.
 */
@Service(value = GCContentApi.class)
@Component(description = "GContent Api", name = "GContent Api", immediate = true, metatype = true)
public final class GCContentApiImpl implements GCContentApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCContentApiImpl.class);

    /**
     * @inheritDoc
     */
    @Override
    public JSONObject me(final GCContext gcContext) throws GCException {
        String content = apiCall("/me", gcContext);
        if ("Invalid credentials.".equals(content)) {
            LOGGER.error("Invalid credentials.");
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
        String extractedResult = jsonNode.get("data").toString();
        List<GCAccount> gcAccountList = JSONUtil.fromJsonToListObject(extractedResult, GCAccount.class);
        return gcAccountList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCProject> projects(final GCContext gcContext, final String accountId) throws GCException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("account_id", accountId));
        String content = apiCall("/projects", gcContext, params);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get("data").toString();
        List<GCProject> gcProjectList = JSONUtil.fromJsonToListObject(extractedResult, GCProject.class);
        return gcProjectList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCTemplate> templates(final GCContext gcContext, final String projectID) throws GCException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("project_id", projectID));
        String content = apiCall("/templates", gcContext, params);

        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get("data").toString();
        List<GCTemplate> templatesList = JSONUtil.fromJsonToListObject(extractedResult, GCTemplate.class);
        return templatesList;
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
        String extractedResult = jsonNode.get("data").toString();
        GCItem item = JSONUtil.fromJsonToObject(extractedResult, GCItem.class);
        return item;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCItem> itemsByProjectId(final GCContext gcContext, final String projectId) throws GCException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("project_id", projectId));
        String content = apiCall("/items", gcContext, params);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get("data").toString();
        List<GCItem> items = JSONUtil.fromJsonToListObject(extractedResult, GCItem.class);
        return items;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCItem> itemsByProjectIdandTemplateId(final GCContext gcContext, final String projectId,
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
        String extractedResult = jsonNode.get("data").toString();
        return JSONUtil.fromJsonToListObject(extractedResult, GCData.class);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<GCFile> filesByItemId(final GCContext gcContext, final String itemId) throws GCException {
        String content = apiCall("/items/" + itemId + "/files", gcContext);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get("data").toString();
        List<GCFile> gcFileList = JSONUtil.fromJsonToListObject(extractedResult, GCFile.class);
        return gcFileList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean updateItemStatus(final GCContext gcContext, final String itemId, final String statusId)
            throws GCException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("status_id", statusId));
        return apiPostCall("/items/" + itemId + "/choose_status", gcContext, params);
    }

    private String apiCall(final String url, final GCContext gcContext) throws GCException {
        return apiCall(url, gcContext, null);
    }

    private Boolean apiPostCall(final String url, final GCContext gcContext, final List<NameValuePair> params) {
        HttpUriRequest httpUriRequest;
        HttpPost httpPost = new HttpPost(gcContext.getApiURL() + url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        httpUriRequest = httpPost;

        HttpClient httpClient = setHeadersAndAuth(httpUriRequest, gcContext);
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            sw.stop();
            LOGGER.debug("Execution time: POST CALL to GC URL {} - {} ms", httpUriRequest.getURI(), sw.getTime());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_ACCEPTED) {
                return true;
            } else {
                LOGGER.error("Request to GatherContent URL: {} failed. Response: {}",
                        httpUriRequest.getURI(), httpResponse.toString());
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Request to GatherContent URL: {} failed. {}", httpUriRequest.getURI(), e.getMessage());
        }
        return false;
    }

    private String apiCall(final String url, final GCContext gcContext, final List<NameValuePair> params)
            throws GCException {
        StringBuilder requestUrl = new StringBuilder(gcContext.getApiURL()).append(url);
        if (params != null) {
            String querystring = URLEncodedUtils.format(params, "utf-8");
            requestUrl.append("?");
            requestUrl.append(querystring);
        }
        HttpUriRequest httpUriRequest = new HttpGet(requestUrl.toString());

        HttpClient httpClient = setHeadersAndAuth(httpUriRequest, gcContext);

        try {
            StopWatch sw = new StopWatch();
            sw.start();
            HttpResponse httpResponse = httpClient.execute(httpUriRequest);
            sw.stop();
            LOGGER.debug("Execution time: GET CALL to GC URL {} - {} ms", httpUriRequest.getURI(), sw.getTime());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(httpResponse.getEntity().getContent(), "UTF-8");
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

    private HttpClient setHeadersAndAuth(final HttpUriRequest httpUriRequest, final GCContext gcContext) {
        for (Map.Entry<String, String> entry : gcContext.getHeaders().entrySet()) {
            Header header = new BasicHeader(entry.getKey(), entry.getValue());
            httpUriRequest.setHeader(header);
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        String username = gcContext.getUsername();
        String apikey = gcContext.getApikey();
        if (username != null && apikey != null) {
            //this is hack for httpclient 4.3.4
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, apikey);
            httpUriRequest.addHeader(BasicScheme.authenticate(creds, "US-ASCII", false));

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, apikey));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        return httpClientBuilder.build();
    }
}
