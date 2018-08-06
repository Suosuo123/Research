package com.bx.research.net;

import android.content.Context;


import com.bx.research.MainApplication;
import com.bx.research.R;
import com.bx.research.constants.ConstantsData;
import com.bx.research.utils.log.LogUtils;
import com.bx.research.widget.WinToast;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Network {

    public static final int PAGE_RANGE = 10;
    public static final int PAGE_RANGE_MAX = 200;
    public static final int RESOUT_OK = 200;

    /**
     * 普通请求方式
     *
     * @param url
     * @param paramsMap
     * @param paramsPosition
     * @param callback
     */
    public static void postNetwork(String url, Map<String, String> paramsMap, RequestParamsPostion paramsPosition,
                                   RequestCallBack<String> callback) {
        LogUtils.d("post_url:====>>>" + url);

        if (!NetworkUtils.isNetConnected(MainApplication.getInstance())) {
            WinToast.toast(MainApplication.getInstance(), R.string.net_remind);
            return;
        }
        RequestParams params = setRequestParams(paramsMap, paramsPosition);

        HttpUtils http = new HttpUtils();
        http.configTimeout(5 * 1000);
        http.configSoTimeout(15 * 1000);
        http.send(HttpRequest.HttpMethod.POST, url, params, callback);
    }

    /**
     * https方式请求(已添加默认证书) 证书，私钥放到assets目录下
     *
     * @param url
     * @param paramsMap
     * @param paramsPosition
     * @param callback
     */
    public static void postNetworkOfHttps(String url, Map<String, String> paramsMap,
                                          RequestParamsPostion paramsPosition, RequestCallBack<String> callback) {
        postNetworkOfHttps(url, paramsMap, paramsPosition, "dlt_1.p12", "client_1.truststore", "secret", callback);
    }

    /**
     * https方式请求
     *
     * @param url
     * @param paramsMap
     * @param paramsPosition
     * @param identifyKeyFileName
     * @param trustKeyFileName
     * @param keyPass
     * @param callback
     */
    public static void postNetworkOfHttps(String url, Map<String, String> paramsMap,
                                          RequestParamsPostion paramsPosition, String identifyKeyFileName, String trustKeyFileName, String keyPass,
                                          RequestCallBack<String> callback) {
        LogUtils.d("post_url:====>>>" + url);

        if (!NetworkUtils.isNetConnected(MainApplication.getInstance())) {
            WinToast.toast(MainApplication.getInstance(), R.string.net_remind);
            return;
        }
        RequestParams params = setRequestParams(paramsMap, paramsPosition);
        SSLSocketFactory socketFactory = SSLCustomSocketFactory.getSocketFactory(
                getInputStreamFromAssets(MainApplication.getInstance(), trustKeyFileName),
                getInputStreamFromAssets(MainApplication.getInstance(), identifyKeyFileName), keyPass);
        HttpUtils http = new HttpUtils();
        http.configTimeout(5 * 1000);
        http.configSoTimeout(15 * 1000);
        if (null != socketFactory) {
            http.configSSLSocketFactory(socketFactory);
        }
        http.send(HttpRequest.HttpMethod.POST, url, params, callback);
    }

    /**
     * 上传文件
     *
     * @param requestURL
     * @param paramsMap
     * @param picPaths
     * @param callback
     */
    public static void uploadFile(String requestURL, Map<String, String> paramsMap,
                                  RequestParamsPostion paramsPosition, String[] picPaths, RequestCallBack<String> callback) {

        if (!NetworkUtils.isNetConnected(MainApplication.getInstance())) {
            WinToast.toast(MainApplication.getInstance(), R.string.net_remind);
            return;
        }
        RequestParams params = setRequestParams(paramsMap, paramsPosition);
        // 添加文件
        if (null != picPaths && picPaths.length != 0) {
            for (int i = 0; i < picPaths.length; i++) {
                params.addBodyParameter("file" + i, new File(picPaths[i]));
            }
        }

        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.POST, requestURL, params, callback);
    }

    /**
     * 添加请求的基本参数
     *
     * @param paramsMap
     * @param paramPosition
     * @return
     */
    private static RequestParams setRequestParams(Map<String, String> paramsMap, RequestParamsPostion paramPosition) {
        RequestParams params = new RequestParams();
        // params.addHeader("Content-Type", "application/json");
        if (null == paramsMap) {
            paramsMap = new HashMap<>();
        }
        addCommonParams(paramsMap);

        // 添加基本参数
        if (null != paramsMap && paramsMap != null) {
            if (paramPosition == RequestParamsPostion.PARAMS_POSITION_BODY) {// 添加至body中
                for (Entry<String, String> ent : paramsMap.entrySet()) {
                    params.addBodyParameter(ent.getKey(), ent.getValue());
                    LogUtils.d("post_params:====>>>" + ent.getKey() + "====>>>" + ent.getValue());
                }
            } else {// 添加至header中
                for (Entry<String, String> ent : paramsMap.entrySet()) {
                    params.addHeader(ent.getKey(), ent.getValue());
                    LogUtils.d("post_params:====>>>" + ent.getKey() + "====>>>" + ent.getValue());
                }
            }
        }

        return params;
    }

    /**
     * 添加一些请求需要用到的公共参数
     *
     * @param paramsMap
     */
    private static void addCommonParams(Map<String, String> paramsMap) {
        paramsMap.put("deviceType", ConstantsData.DEVICE_TYPE + "");
        // paramsMap.put("imei", "123");
        // paramsMap.put("providername", "123");
        // paramsMap.put("model", "123");
        // paramsMap.put("system", "123");
        // paramsMap.put("channel", "123");
        // paramsMap.put("versionname", "123");
    }

    /**
     * 从assets中读取
     *
     * @param context
     * @param fileName
     * @return
     */
    private static InputStream getInputStreamFromAssets(Context context, String fileName) {
        InputStream is = null;
        try {
            is = context.getApplicationContext().getAssets().open(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * 按行读取txt
     *
     * @param is
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private String readTextFromSDcard(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuffer buffer = new StringBuffer("");
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
