package com.junar.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.http.AndroidHttpClient;
import android.util.Log;

public class JunarAPI {
    private final String API_KEY = "";
    private final String BASE_URI = "http://api.recursos.datos.gob.cl";
    private final String DS_URI = "/datastreams/";
    private final String INVOKE_URI = DS_URI.concat("invoke/");
    private String OUTPUT = "json_array";
    public static final String PARSE_APP_ID = "";
    public static final String PARSE_REST_API = "";
    // Set the timeout in milliseconds until a connection is established.
    private static final int timeoutConnection = 30000;
    // Set the default socket timeout (SO_TIMEOUT)
    // in milliseconds which is the timeout for waiting for data.
    private static final int timeoutSocket = 30000;
    private static final String TAG = JunarAPI.class.getSimpleName();

    public String getApiKey() {
        return this.API_KEY;
    }

    private String getURI(String guid, String action) {
        return this.BASE_URI.concat(action).concat(guid).concat("?auth_key=")
                .concat(getApiKey()).concat(getOutputForUrl());
    }

    private String getURI(String guid, String action, String[] arguments,
            String[] filters, int limit, int page, long timestamp) {
        String localUri = getURI(guid, action);

        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                localUri = localUri.concat("&pArgument" + i + "="
                        + arguments[i]);
            }
        }

        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                if (filters[i].contains("where=")) {
                    localUri = localUri.concat("&" + filters[i]);
                } else {
                    localUri = localUri
                            .concat("&filter" + i + "=" + filters[i]);
                }
            }
        }

        if (limit >= 0) {
            localUri = localUri.concat("&limit=" + limit);
        }

        if (page >= 0) {
            localUri = localUri.concat("&page=" + page);
        }

        if (timestamp >= 0) {
            localUri = localUri.concat("&if_modified_since=" + timestamp);
        }

        return localUri;
    }

    public String invoke(String guid, String[] params, String[] filters,
            int limit, int page, long timestamp) {
        String url = getURI(guid, INVOKE_URI, params, filters, limit, page,
                timestamp);
        return callURI(url);
    }

    public String invoke(String guid) {
        String url = getURI(guid, INVOKE_URI, null, null, -1, -1, -1);
        return callURI(url);
    }

    public String invoke(String guid, String[] params, boolean hasFilter) {
        String url = null;
        if (hasFilter) {
            getURI(guid, INVOKE_URI, null, params, -1, -1, -1);
        } else {
            getURI(guid, INVOKE_URI, params, null, -1, -1, -1);
        }
        return callURI(url);
    }

    public String info(String guid) {
        String url = getURI(guid, this.DS_URI);

        return callURI(url);
    }

    public void setOutput(String type) {
        this.OUTPUT = type;
    }

    public String getOutputForUrl() {
        return "&output=".concat(this.OUTPUT);
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private String callURI(String url) {
        String resp = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            Log.i(TAG, url);
            HttpGet request = new HttpGet(url);
            AndroidHttpClient.modifyRequestToAcceptGzipResponse(request);
            HttpResponse response;
            response = httpClient.execute(request);
            InputStream inputStream = AndroidHttpClient
                    .getUngzippedContent(response.getEntity());

            resp = convertStreamToString(inputStream);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return resp;
    }
}
