/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services.impl;

import com.adobe.granite.license.ProductInfoService;
import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.*;
import com.axamit.gc.api.services.GCContentNewApi;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.GCUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * OSGI service implements <tt>GCContentApi</tt> interface provides methods to get information from remote
 * GatherContent server.
 *
 * @author Axamit, gc.support@axamit.com
 */
@Service(value = GCContentNewApi.class)
@Component(description = "GContent New Api", name = "GContent New Api", immediate = true, metatype = true)
public final class GCContentNewApiImpl implements GCContentNewApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCContentNewApiImpl.class);
    private static final String SUCCESS_POST_CALL_FLAG = "success";
    private static final String JSON_DATA_NODE_NAME = "data";
    private static final String JSON_META_NODE_NAME = "meta";
    private static final String ID_PARAMETER = "id";

    private static final String TEMPLATE_BY_ID = "/templates/%s";
    private static final String ITEMS_BY_ID = "/items/%s";
    private static final String TEMPLATES_BY_PROJECT_ID = "/projects/%s/templates";
    private static final String FOLDERS_BY_PROJECT_ID = "/projects/%s/folders";
    private static final String ITEMS_BY_PROJECT_ID = "/projects/%s/items";
    private static final String UPDATE_ITEM_BY_ID = "/items/%s/content";
    private static final String PARAM_PROJECT_ID = "project_id";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_TEMPLATE_ID = "template_id";
    public static final String STRUCTURE = "structure";
    public static final String STATUS_ID = "status_id";
    public static final String FOLDER_UUID = "folder_uuid";
    public static final String POSITION = "position";
    public static final String CONTENT = "content";

    static final Pattern DOUBLE_QUOTES_PATTERN = Pattern.compile("^\"|\"$");

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
     * @inheritDoc API v2.0
     */
    @Override
    public GCTemplate template(final GCContext gcContext, final Integer templateId) throws GCException {
        String content = GCUtil.apiCall(String.format(TEMPLATE_BY_ID, templateId), gcContext, userAgentInfo, true);
        return JSONUtil.fromJsonToObject(content, GCTemplate.class);
    }

    /**
     * @inheritDoc API v2.0
     */
    @Override
    public List<GCTemplateData> templates(final GCContext gcContext, final Integer projectID) throws GCException {
        String content = GCUtil.apiCall(String.format(TEMPLATES_BY_PROJECT_ID, projectID), gcContext, userAgentInfo, true);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return ImmutableList.copyOf(JSONUtil.fromJsonToListObject(extractedResult, GCTemplateData.class));
    }

    /**
     * @inheritDoc API v2.0
     */
    @Override
    public GCItem itemById(final GCContext gcContext, final Integer itemId) throws GCException {
        String content = GCUtil.apiCall(String.format(ITEMS_BY_ID, itemId), gcContext, userAgentInfo, true);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return JSONUtil.fromJsonToObject(extractedResult, GCItem.class);
    }

    /**
     * @inheritDoc API v2.0
     */
    @Override
    public List<GCItem> itemsByProjectId(final GCContext gcContext, final Integer projectId) throws GCException {
        String content = GCUtil.apiCall(String.format(ITEMS_BY_PROJECT_ID, projectId), gcContext, userAgentInfo, true);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return ImmutableList.copyOf(JSONUtil.fromJsonToListObject(extractedResult, GCItem.class));
    }

    /**
     * @inheritDoc API v2.0
     */
    @Override
    public int createItem(final GCItem gcItem, final GCContext gcContext) throws GCException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_NAME, gcItem.getName()));
        params.add(new BasicNameValuePair(PARAM_TEMPLATE_ID, Integer.toString(gcItem.getTemplateId())));
        if (gcItem.getTemplateId() == 0) {
            GCTemplateStructure gcTemplateStructure = template(gcContext, gcItem.getTemplateId()).getRelated().getStructure();
            if (gcTemplateStructure != null && gcTemplateStructure.getGroups() != null) {
                params.add(new BasicNameValuePair(STRUCTURE, JSONUtil.fromObjectToJsonString(gcTemplateStructure)));
            }
        }
        if(gcItem.getStatusId() != null){
            params.add(new BasicNameValuePair(STATUS_ID, Integer.toString(gcItem.getStatusId())));
        }
        if(StringUtils.isNotBlank(gcItem.getFolderUuid())){
            params.add(new BasicNameValuePair(FOLDER_UUID, gcItem.getFolderUuid()));
        }
        if(gcItem.getPosition() != null){
            params.add(new BasicNameValuePair(POSITION, Integer.toString(gcItem.getPosition())));
        }
        if(!gcItem.getContent().isEmpty()) {
            Map<String, GCContent> gcContentMap = gcItem.getContent();
            Map<String, String> stringMap = new HashMap<>();
                    gcContentMap.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                    .forEach(entry -> stringMap.put(entry.getKey(), entry.getValue().getText()));
                    String contentJson = DOUBLE_QUOTES_PATTERN.matcher(JSONUtil.fromObjectToJsonString(stringMap)).replaceAll("");
            params.add(new BasicNameValuePair(CONTENT, contentJson));
        }

        Map<String, String> returnObject = apiPostCall("/projects/" + gcItem.getProjectId() + "/items", gcContext, params);
        int id = 0;
        if(GCStringUtil.getLastURLPartOrNull(returnObject.get(ID_PARAMETER)) != null){
            id = Integer.parseInt(GCStringUtil.getLastURLPartOrNull(returnObject.get(ID_PARAMETER)));
        }
        return Boolean.parseBoolean(returnObject.get(SUCCESS_POST_CALL_FLAG)) ? id : 0;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean updateItemContent(final GCItem gcItem, final GCContext gcContext) {
        StringEntity stringEntity = buildHttpUpdateEntity(gcItem);
        return BooleanUtils.toBoolean(apiPostCall(String.format(UPDATE_ITEM_BY_ID, gcItem.getId()), gcContext, stringEntity)
                .get(SUCCESS_POST_CALL_FLAG));
    }

    /**
     * @return
     * @inheritDoc API v2.0
     */
    @Override
    public List<GCFolder> foldersByProjectId(final GCContext gcContext, final Integer projectId) throws GCException {
        String content = GCUtil.apiCall(String.format(FOLDERS_BY_PROJECT_ID, projectId), gcContext, userAgentInfo, true);
        JsonNode jsonNode = JSONUtil.fromJsonToJSonNode(content);
        String extractedResult = jsonNode.get(JSON_DATA_NODE_NAME).toString();
        return ImmutableList.copyOf(JSONUtil.fromJsonToListObject(extractedResult, GCFolder.class));
    }

    private StringEntity buildHttpUpdateEntity(final GCItem gcItem) {
        String stringEntity = StringUtils.EMPTY;
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            stringEntity = objectMapper.writeValueAsString(gcItem);

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed create JSON Object", e);
        }

        return new StringEntity(stringEntity, ContentType.APPLICATION_JSON);
    }

    private static Map<String, String> apiPostCall(final String url, final GCContext gcContext,
                                                   final List<NameValuePair> params) {
        UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
        return apiPostCall(url, gcContext, httpEntity);
    }

    /**
     * @return
     * @inheritDoc API v2.0
     */
    private static <T extends StringEntity> Map<String, String> apiPostCall(final String url, final GCContext gcContext,
                                                                            final T httpEntity) {
        HttpPost httpPost = new HttpPost(gcContext.getApiURL() + url);
        httpPost.setEntity(httpEntity);

        Map<String, String> returnObject = new HashMap<>();

        HttpClient httpClient = GCUtil.setHeadersAndAuth(httpPost, gcContext, userAgentInfo, true);
        try {
            LOGGER.debug("Requested GatherContent URL " + httpPost.getURI()
                    + System.lineSeparator() + "Request method: " + httpPost.getMethod()
                    + System.lineSeparator() + "Request entity: " + EntityUtils.toString(httpPost.getEntity(), StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED) {
                String responseEntityContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                LOGGER.error("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse
                        + System.lineSeparator() + "ResponseEntity: " + responseEntityContent);
            } else {
                String response = EntityUtils.toString(httpResponse.getEntity());
                LOGGER.debug("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse
                        + System.lineSeparator() + "ResponseEntity: " + response, StandardCharsets.UTF_8);
                if (StringUtils.isNotEmpty(response)) {
                    JsonNode responseNode = JSONUtil.fromJsonToJSonNode(response);
                    JsonNode jsonNode = responseNode.get(JSON_DATA_NODE_NAME);
                    if (jsonNode != null) {
                        String id = StringUtils.remove(jsonNode.get(ID_PARAMETER).toString(), "\"");
                        returnObject.put(ID_PARAMETER, id);
                        LOGGER.debug("data param is null, checking meta");
                    } else {
                        jsonNode = responseNode.get(JSON_META_NODE_NAME);
                        if (jsonNode != null) {
                            //do smth later
                        } else {
                            LOGGER.debug("meta param is null");
                        }
                    }
                }
            }
            returnObject.put(SUCCESS_POST_CALL_FLAG, Boolean.toString(statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED));
        } catch (IOException e) {
            LOGGER.error("Request to GatherContent URL: {} failed. {}", httpPost.getURI(), e.getMessage());
        } catch (GCException e) {
            LOGGER.error("Getting ID from HttpResponse failed. {}", e.getMessage());
        }
        return ImmutableMap.copyOf(returnObject);
    }
}
