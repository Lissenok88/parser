package com.lyudmila.topliba;


import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.http.Consts.UTF_8;

public class HttpConnector {

    private static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0";
    private static final String contentType = "application/x-www-form-urlencoded";
    private static HttpClientContext context = HttpClientContext.create();

    public static Document getHtml(String url){
        return getHtml(url, null);
    }

    public static Document getHtml(String url, HttpConnector.ResponseHandler successHandler){
        HttpConnector.Response response = HttpConnector.get(url, successHandler);
        return Jsoup.parse(response.body);
    }

    private static CloseableHttpClient getNewClient() {
        return HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().
                        setCookieSpec(CookieSpecs.STANDARD).build()
                ).build();
    }

    public static Response get(String url) {
        return get(url, null);
    }

    public static Response get(String url, ResponseHandler successHandler) {
        if (successHandler == null) {
            return getInternal(url);
        }
        Response response = null;
        for (int i = 0; i < 10; i++) {
            Response result = getInternal(url);
            if (successHandler.HandleResponse(result)) {
                return result;
            }
        }
        return response;
    }

    private static Response getInternal(String url) {
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", userAgent);
        Response responseModel = new Response();
        responseModel.success = true;
        try (
                CloseableHttpClient client = getNewClient();
                CloseableHttpResponse response = client.execute(request, context)
        ) {
            responseModel.statusCode = response.getStatusLine().getStatusCode();
            InputStream responseStream = response.getEntity().getContent();

            responseModel.body = new BufferedReader(new InputStreamReader(responseStream)).lines()
                    .parallel().collect(Collectors.joining("\n"));
        } catch (ClientProtocolException e) {
            Console.output(e.getMessage(), true);
            e.printStackTrace();
            responseModel.success = false;
            responseModel.ex = new RuntimeException(e);
        } catch (IOException e) {
            Console.output(e.getMessage(), true);
            responseModel.success = false;
            responseModel.ex = new RuntimeException(e);
        }
        Document document = Jsoup.parse(responseModel.body);
        responseModel.document = document;

        return responseModel;
    }

    public static Response post(String url, HashMap<String, String> body) {
        return post(url, body, null);
    }

    public static Response post(String url, HashMap<String, String> body, ResponseHandler successHandler) {
        if (successHandler == null) {
            return postInternal(url, body);
        }
        Response response = null;
        for (int i = 0; i < 10; i++) {
            Response result = postInternal(url, body);
            if (successHandler.HandleResponse(result)) {
                return result;
            }
        }
        return response;
    }

    private static Response postInternal(String url, HashMap<String, String> body) {
        HttpPost request = new HttpPost(url);
        request.addHeader("User-Agent", userAgent);
        request.addHeader("Content-Type", contentType);
        Response responseModel = new Response();
        NameValuePair[] data = new NameValuePair[body.size()];
        int index = 0;
        for (Map.Entry<String, String> entry : body.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            data[index] = new BasicNameValuePair(key, value);
            index++;

        }
        UrlEncodedFormEntity bodyPost = new UrlEncodedFormEntity(Arrays.stream(data).toList(), UTF_8);
        request.setEntity(bodyPost);
        try (
                CloseableHttpClient client = getNewClient();
                CloseableHttpResponse response = client.execute(request, context)
        ) {
            responseModel.statusCode = response.getStatusLine().getStatusCode();
            InputStream responseStream = response.getEntity().getContent();
            responseModel.body = new BufferedReader(new InputStreamReader(responseStream)).lines()
                    .parallel().collect(Collectors.joining("\n"));
        } catch (ClientProtocolException e) {
            Console.output(e.getMessage(), true);
            responseModel.success = false;
            responseModel.ex = new RuntimeException(e);
        } catch (IOException e) {
            Console.output(e.getMessage(), true);
            responseModel.success = false;
            responseModel.ex = new RuntimeException(e);
        }

        Document document = Jsoup.parse(responseModel.body);
        responseModel.document = document;

        return responseModel;
    }

    public static class Response {
        public int statusCode;
        public String body;
        public Document document;
        public boolean success;
        public RuntimeException ex;
    }

    public interface ResponseHandler {
        boolean HandleResponse(Response response);
    }
}
