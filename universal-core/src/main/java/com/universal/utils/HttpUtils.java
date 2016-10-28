package com.universal.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.universal.exception.UnexpectedHttpStatusCodeException;

public final class HttpUtils {

    public static final int URL_ENCODE = 1;
    public static final int JS_URL_ENCODE = 2;

    private final static int TIMEOUT = 10000;
    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    static {
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(10);
    }

    public static Map<String, String> parseQueryString(final String quereyString) {

        final Map<String, String> resultMap = new HashMap<>();

        if (!StringUtils.isBlank(quereyString)) {

            String[] blocks = StringUtils.split(quereyString, "&");

            for (String block : blocks) {

                String[] kv = StringUtils.split(block, "=");
                if (kv.length == 2) {

                    resultMap.put(kv[0], kv[1]);
                }
            }
        }

        return resultMap;
    }

    public static String generateQueryString(final Map<String, String> map) {

        return generateQueryString(map, -1);
    }

    public static String generateQueryString(final Map<String, String> map, final int urlEncodeMode) {

        String queryString = StringUtils.EMPTY;
        for (Map.Entry<String, String> entry : map.entrySet()) {

            queryString += entry.getKey() + "=";

            if (urlEncodeMode >= URL_ENCODE && StringUtils.isNotEmpty(entry.getValue())) {

                String encodedValue = StringUtils.EMPTY;

                try {
                    encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }

                if (urlEncodeMode == JS_URL_ENCODE) {
                    encodedValue = encodedValue.replaceAll("\\+", "%20").replaceAll("\\%21", "!").replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                            .replaceAll("\\%7E", "~");
                }

                queryString += encodedValue;

            } else {
                queryString += entry.getValue();
            }

            queryString += "&";
        }

        return StringUtils.removeEnd(queryString, "&");
    }

    public static String get(final String url) throws UnexpectedHttpStatusCodeException {

        return get(url, TIMEOUT);
    }

    public static String get(final String url, final int timeoutSeconds) throws UnexpectedHttpStatusCodeException {

        final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeoutSeconds).setConnectTimeout(timeoutSeconds).setConnectionRequestTimeout(timeoutSeconds)
                .build();

        HttpUriRequest httpRequest = RequestBuilder.get().setUri(url).setConfig(requestConfig).build();

        return execute(url, httpRequest);
    }

    private static String execute(final String url, final HttpUriRequest request) throws UnexpectedHttpStatusCodeException {

        final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

        try {

            final CloseableHttpResponse httpResponse = httpClient.execute(request);

            final StatusLine httpStatus = httpResponse.getStatusLine();
            final String response = EntityUtils.toString(httpResponse.getEntity());

            if (httpStatus.getStatusCode() != 200) {
                throw new UnexpectedHttpStatusCodeException(httpStatus.getStatusCode(), response);
            }

            return response;

        } catch (ParseException | IOException e) {
            logger.error("Http get request failed: " + url, e);
        }

        return StringUtils.EMPTY;
    }

    public static String post(final String url, final Map<String, String> parameters) throws UnexpectedHttpStatusCodeException {

        return post(url, parameters, TIMEOUT);
    }

    public static String post(final String url, final Map<String, String> parameters, final int timeoutSeconds) throws UnexpectedHttpStatusCodeException {

        List<NameValuePair> requestParameters = new ArrayList<>();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            requestParameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeoutSeconds).setConnectTimeout(timeoutSeconds).setConnectionRequestTimeout(timeoutSeconds)
                .build();

        HttpUriRequest httpRequest = RequestBuilder.post().setUri(url).addParameters(requestParameters.toArray(new BasicNameValuePair[0])).setConfig(requestConfig).build();

        return execute(url, httpRequest);
    }

    public static String post(final String url, final String data, final int timeoutSeconds) throws UnexpectedHttpStatusCodeException {

        final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeoutSeconds).setConnectTimeout(timeoutSeconds).setConnectionRequestTimeout(timeoutSeconds)
                .build();

        HttpPost httpRequest = new HttpPost();

        try {
            httpRequest.setURI(new URI(url));
        } catch (URISyntaxException e) {
            logger.error("Invaild URI", e);
            return StringUtils.EMPTY;
        }

        httpRequest.setConfig(requestConfig);
        httpRequest.setEntity(new StringEntity(data, ContentType.create("application/x-www-form-urlencoded", Consts.UTF_8)));

        return execute(url, httpRequest);
    }

    public static String post(final String url, final String data) throws UnexpectedHttpStatusCodeException {

        return post(url, data, TIMEOUT);
    }

    public static String realIp(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");

        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        } else {
            return request.getRemoteAddr();
        }
    }
}
