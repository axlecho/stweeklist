package com.songtaste.weeklist.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by axlecho on 2015/1/20.
 */
public class NetworkUtil {

    private final static int HTTP_CONNECT_TIMEOUT = 120 * 1000;
    private final static int HTTP_SOCKET_TIMEOUT = 120 * 1000;

    private static HttpClient httpClient;
    private static String errorString = "";
    private static List<String> cookies = new ArrayList<>();
    private static NetworkUtil instance;

    public static synchronized NetworkUtil getInstance() {
        if (instance == null) {
            instance = new NetworkUtil();
        }

        return instance;
    }

    private NetworkUtil() {
        httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, HTTP_SOCKET_TIMEOUT);
    }

    public String get(String url) {
        return get(url, null);
    }

    public String get(String url, OnCompletionListener listener) {
        try {
            HttpGet httpGet = new HttpGet(url);
            for (String cookie : cookies) {
                httpGet.addHeader("Cookie", cookie);
            }
            HttpResponse ret = httpClient.execute(httpGet);

            if (listener != null) {
                listener.completion(ret.getHeaders("set-cookie"));
            }

            return EntityUtils.toString(ret.getEntity(), "gb2312");
        } catch (ClientProtocolException e) {
            errorString = "网络错误:不支持的编码";
            e.printStackTrace();
        } catch (IOException e) {
            errorString = "网络错误:IO错误";
            e.printStackTrace();
        }
        return null;
    }

    public String post(String url, List<NameValuePair> params) {
        return post(url, params, null);
    }

    public String post(String url, List<NameValuePair> params, OnCompletionListener listener) {
        try {
            HttpPost httpPost = new HttpPost(url);
            for (String cookie : cookies) {
                httpPost.addHeader("Cookie", cookie);
            }

            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse ret = httpClient.execute(httpPost);

            if (listener != null) {
                listener.completion(ret.getHeaders("set-cookie"));
            }

            return EntityUtils.toString(ret.getEntity(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            errorString = "网络错误:不支持的编码";
            e.printStackTrace();
        } catch (IOException e) {
            errorString = "网络错误:IO错误";
            e.printStackTrace();
        }
        return null;
    }

    public static String getError() {
        return errorString;
    }

    public void addCookie(String cookie) {
        cookies.add(cookie);
    }

    public interface OnCompletionListener {
        public void completion(Header[] cookies);
    }
}
