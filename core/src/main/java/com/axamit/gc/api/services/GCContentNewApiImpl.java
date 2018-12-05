/*
 * Axamit, gc.support@axamit.com
 */

package com.axamit.gc.api.services;

import com.adobe.granite.license.ProductInfo;
import com.adobe.granite.license.ProductInfoService;
import com.axamit.gc.api.GCContext;
import com.axamit.gc.api.dto.GCConfig;
import com.axamit.gc.api.dto.GCElement;
import com.axamit.gc.api.dto.GCItem;
import com.axamit.gc.api.dto.GCOption;
import com.axamit.gc.core.exception.GCException;
import com.axamit.gc.core.util.GCStringUtil;
import com.axamit.gc.core.util.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String PARAM_PARENT_ID = "parent_id";
    private static final String PARAM_PROJECT_ID = "project_id";
    private static final String PARAM_TEMPLATE_ID = "template_id";
    private static final String PARAM_CONTENT = "content[%s]";
    private static final String PARAM_CONTENT_JSON = "content";
    private static final String PARAM_CONTENT_MULTIVALUE = "content[%s][][id]";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_ROOT = "root";
    private static final String JSON_DATA_NODE_NAME = "data";
    private static final String ID_PARAMETER = "id";
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
    public String createItem(final GCItem gcItem, final GCContext gcContext) throws GCException {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PARAM_PROJECT_ID, gcItem.getProjectId()));
        params.add(new BasicNameValuePair(PARAM_NAME, gcItem.getName()));
        if (StringUtils.isNotEmpty(gcItem.getParentId()) && !StringUtils.equals(gcItem.getParentId(), PARAM_ROOT)) {
            params.add(new BasicNameValuePair(PARAM_PARENT_ID, gcItem.getParentId()));
        }
        if (StringUtils.isNotEmpty(gcItem.getTemplateId())) {
            params.add(new BasicNameValuePair(PARAM_TEMPLATE_ID, gcItem.getTemplateId()));
        }
        List<GCConfig> gcConfigs = gcItem.getConfig();
        if (gcConfigs != null && gcConfigs.size() != NumberUtils.INTEGER_ZERO) {
            addNewEditorRequestParams(params, gcConfigs);
        }
        Map<String, String> returnObject = newApiPostCall("/items/create/", gcContext, params);
        String id = GCStringUtil.getLastURLPartOrNull(returnObject.get(ID_PARAMETER));
        if (Boolean.parseBoolean(returnObject.get(SUCCESS_POST_CALL_FLAG))) {
            return id;
        }
        return null;
    }

    private void addNewEditorRequestParams(final List<NameValuePair> params, final List<GCConfig> gcConfigs) {
        for (GCConfig gcConfig : gcConfigs) {
            for (GCElement element : gcConfig.getElements()) {
                if (element.getOptions() == null) {
                    if (StringUtils.isNotEmpty(element.getValue())) {
                        params.add(new BasicNameValuePair(String.format(PARAM_CONTENT, element.getName()), element.getValue()));
                    }
                } else {
                    for (GCOption option : element.getOptions()) {
                        if (option.getSelected()) {
                            params.add(new BasicNameValuePair(String.format(PARAM_CONTENT_MULTIVALUE, element.getName()), option.getName()));
                        }
                    }
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Boolean updateItem(final List<GCConfig> gcConfigs, final String itemId, final GCContext gcContext) {
        Boolean result;
        StringEntity stringEntity = buildHttpUpdateEntity(gcConfigs);
        result = Boolean.parseBoolean(newApiPostCall("/items/" + itemId + "/update-content", gcContext, stringEntity)
                .get(SUCCESS_POST_CALL_FLAG));
        return result;
    }

    private StringEntity buildHttpUpdateEntity(final List<GCConfig> gcConfigs) {
        String stringEntity = StringUtils.EMPTY;
        try {
            JSONObject configJsonObject = new JSONObject();
            for (GCConfig gcConfig : gcConfigs) {
                for (GCElement element : gcConfig.getElements()) {
                    if (element.getOptions() == null) {
                        if (StringUtils.isNotEmpty(element.getValue())) {

                            configJsonObject.put(element.getName(), element.getValue());

                        }
                    } else {
                        JSONArray jsonArray = new JSONArray();
                        for (GCOption option : element.getOptions()) {
                            if (option.getSelected()) {
                                jsonArray.put(new JSONObject().put(ID_PARAMETER, option.getName()));
                            }
                        }
                        configJsonObject.put(element.getName(), jsonArray);
                    }
                }
            }

            stringEntity = new JSONObject().put(PARAM_CONTENT_JSON, configJsonObject).toString();
        } catch (JSONException e) {
            LOGGER.error("Failed create JSON Object", e);
        }

        return new StringEntity(stringEntity, ContentType.APPLICATION_JSON);
    }

    private static Map<String, String> newApiPostCall(final String url, final GCContext gcContext,
                                                      final List<NameValuePair> params) {
        UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
        return newApiPostCall(url, gcContext, httpEntity);
    }


    private static <T extends StringEntity> Map<String, String> newApiPostCall(final String url, final GCContext gcContext,
                                                                               final T httpEntity) {
        HttpPost httpPost = new HttpPost(gcContext.getApiURL() + url);
        httpPost.setEntity(httpEntity);
        Map<String, String> returnObject = new HashMap<>();

        HttpClient httpClient = setNewHeadersAndAuth(httpPost, gcContext);
        try {
            LOGGER.debug("Requested GatherContent URL " + httpPost.getURI()
                    + System.lineSeparator() + "Request method: " + httpPost.getMethod()
                    + System.lineSeparator() + "Request entity: " + EntityUtils.toString(httpPost.getEntity(), StandardCharsets.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED) {
                String responseEntityContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                LOGGER.error("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse.toString()
                        + System.lineSeparator() + "ResponseEntity: " + responseEntityContent);
            } else {
                String response = EntityUtils.toString(httpResponse.getEntity());
                LOGGER.debug("Requested GatherContent URL: " + httpPost.getURI()
                        + System.lineSeparator() + "Response: " + httpResponse.toString()
                        + System.lineSeparator() + "ResponseEntity: " + response, StandardCharsets.UTF_8);
                if (StringUtils.isNotEmpty(response)) {
                    JsonNode responseNode = JSONUtil.fromJsonToJSonNode(response);
                    String id = StringUtils.remove(responseNode.get(JSON_DATA_NODE_NAME).get(ID_PARAMETER).toString(), "\"");
                    returnObject.put(ID_PARAMETER, id);
                }
            }
            returnObject.put(SUCCESS_POST_CALL_FLAG, Boolean.toString(statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED));
        } catch (IOException e) {
            LOGGER.error("Request to GatherContent URL: {} failed. {}", httpPost.getURI(), e.getMessage());
        } catch (GCException e) {
            LOGGER.error("Getting ID from HttpResponse failed. {}", e.getMessage());
        }
        return returnObject;
    }

    private static HttpClient setNewHeadersAndAuth(final HttpRequest httpUriRequest, final GCContext gcContext) {
        for (Map.Entry<String, String> entry : gcContext.getNewEditorHeaders().entrySet()) {
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
}
